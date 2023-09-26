package DataPathSSP

// Optimized counter example
import chisel3._
import chisel3.util._

object var_size
{
    val counter_size = 1024
    val load_B = 0.U
    val load_H = 1.U
    val load_W = 2.U
    val load_BU = 4.U
    val load_HU = 5.U
    val store_B = 0.U
    val store_H = 1.U
    val store_W = 2.U
    val lui     = "h37".U
    val SB_type = "h63".U
    val auipc   = "h17".U
    val unb_ins   = "h6f".U
    val jalr    = "h67".U
}
import var_size._
class Top extends Module {
    val io = IO(new Bundle {
        val ins = Input (UInt(32.W))   // taking input for the instruction in the IMEM
        val inst_out = Output(UInt(32.W))
        val out = Output(SInt(32.W))
    })

    // val imem_module = Module(new IMEM("D:/Semester lll/CO&AL(P)/assemblyHexcode.txt"))
    val imem_module     = Module(new IMEM())
    val pc_module       = Module(new PC(counter_size))
    val register_module = Module(new RegisterFile)
    val alu_module      = Module(new ALU)
    val dmem_module     = Module(new DMEM)
    val cu_module       = Module(new CU)
    val branch_module   = Module(new Branch) 

    // If we only give default value for the Output pin but does connect it with other module 
    // we will get an error "java.util.NoSuchElementException: key not found: IMEM.async_mem"
    // io.out := 0.S

    imem_module.io.wen := cu_module.io.imem_en        // Setting enable pin for IMEM

    // alu_module.io.alu_Op := 0.U
    // alu_module.io.in_A := (pc_module.io.out).asUInt
    // alu_module.io.in_B := 4.U
    // pc_module.io.br_imm := (alu_module.io.out).asSInt
    
    imem_module.io.address := (pc_module.io.out).asUInt
    pc_module.io.jalr_en_pc := cu_module.io.jalr_en           // Enabling PC jalr through CU

    io.inst_out := io.ins
    imem_module.io.dataIn := io.inst_out  // assigning the output to dataIn of IMEM

    pc_module.io.br_imm := 0.S         // default value for branch immediate

    // val again_ins = RegInit(false.B  )
    // when(imem_module.io.dataOut (6,0) === 3.U || imem_module.io.dataOut (6,0) === "h23".U)
    // {
    //     when(again_ins === false.B)
    //     {
    //         pc_module.io.jalr_en_pc := 1.B
    //         pc_module.io.br_imm := (pc_module.io.out).asSInt  // same instruction address
    //     }
    // }

    // cu_module.io.ins := imem_module.io.dataOut        // Giving whole instruction to CU
    // when(again_ins === true.B)  // true means we are exucuting second time
    // {
    //     cu_module.io.ins := 0.U
    // }.elsewhen((imem_module.io.dataOut (6,0) === 3.U || imem_module.io.dataOut (6,0) === "h23".U) && !again_ins)
    // {
    //     again_ins := true.B
    // }

    //  when(cu_module.io.ins === 0.U)
    // {
    //     again_ins := false.B
    // }
    
    cu_module.io.ins := imem_module.io.dataOut        // Giving whole instruction to CU

    register_module.io.raddr1 := cu_module.io.rs1     // Input of address of source register
    register_module.io.raddr2 := cu_module.io.rs2

    alu_module.io.alu_Op := cu_module.io.func3_7    // assigning opcode from CU

    alu_module.io.in_A := 0.U  // Default value to ALU
    alu_module.io.in_B := 0.U

    when(imem_module.io.dataOut(6,0) === "h33".U) 
    {
        alu_module.io.in_A := register_module.io.rdata1  // Inserting data from register file to ALU
        alu_module.io.in_B := register_module.io.rdata2
    }.elsewhen(imem_module.io.dataOut(6, 0) === "h13".U)
    {
        alu_module.io.in_A := register_module.io.rdata1  // Inserting data from register file to ALU
        alu_module.io.in_B := cu_module.io.immediate
    }
    


    //Data Memory code
    dmem_module.io.addr := 0.U    // default value to data memory address
    dmem_module.io.rd_enable := cu_module.io.r_en
    // dmem_module.io.func3  := cu_module.io.func3_7  // Assigning function 3 of the DMEM
    // when(cu_module.io.ins(6,0) === 3.U) // calculating address in load instruction
    // {
    //     // Using ALU to calculate load address
    //     alu_module.io.alu_Op := 0.U
    //     alu_module.io.in_A := cu_module.io.immediate
    //     alu_module.io.in_B := register_module.io.rdata1
    //     dmem_module.io.addr :=  alu_module.io.out(9,2)
    // }
    // .elsewhen(cu_module.io.ins(6,0) === "h23".U)  // Calculating address in store instruction
    // {
    //     // Using ALU to calculate store address
    //     alu_module.io.alu_Op := 0.U
    //     alu_module.io.in_A := cu_module.io.immediate
    //     alu_module.io.in_B := register_module.io.rdata1  // write according to S-format
    //     val temp_addr   = Cat( Fill( 2,(alu_module.io.out)(31) ), (alu_module.io.out)(31,2) )
    //     dmem_module.io.addr := temp_addr(7,0)

    // }
    register_module.io.wen := cu_module.io.w_en   // write enable register
    register_module.io.waddr := cu_module.io.rd  //rd address
    register_module.io.wdata := alu_module.io.out  // write data from alu module

    branch_module.io.func3 := cu_module.io.func3_7   // setting branch instruction function 3
    branch_module.io.br_ins := cu_module.io.br_en    // setting that a branch instruction has come
    // Values for branch module inputs
    branch_module.io.inpA := register_module.io.rdata1
    branch_module.io.inpB := register_module.io.rdata2

    
    // pc_module.io.en_jump := branch_module.io.br_taken  
    pc_module.io.en_jump := cu_module.io.unbr_en  // For Both Jal and Jalr   
    // This line was not letting to execute the branch instruction
    // so had to write the branch statement inside the condition
    when(cu_module.io.ins(6,0) === SB_type)
    {
        pc_module.io.en_jump := branch_module.io.br_taken  
    }


    when(cu_module.io.ins(6,0) === lui)    
    {   
        // Assigning upper immediate to register file
        register_module.io.wdata := cu_module.io.immediate
    }.elsewhen(cu_module.io.ins(6,0) === auipc )
    {
        alu_module.io.in_A := (pc_module.io.out).asUInt
        alu_module.io.in_B := cu_module.io.immediate
        register_module.io.wdata := alu_module.io.out
    }.elsewhen(branch_module.io.br_ins)   // checking if there is a branch instruction
    {
        pc_module.io.en_jump := branch_module.io.br_taken
        
    //    when(branch_module.io.br_taken)
    //    {
    //         pc_module.io.br_imm := (cu_module.io.immediate).asSInt
    //    }
        alu_module.io.in_A := (pc_module.io.out).asUInt
        alu_module.io.in_B := cu_module.io.immediate
        alu_module.io.alu_Op := 0.U
        pc_module.io.br_imm := (alu_module.io.out).asSInt

    }.elsewhen(cu_module.io.ins(6,0) === unb_ins)   // Unconditional branch jal instruction
    {
        alu_module.io.in_A := (pc_module.io.out).asUInt
        alu_module.io.in_B := cu_module.io.immediate
        alu_module.io.alu_Op := 0.U
        pc_module.io.br_imm   := (alu_module.io.out).asSInt
        // pc_module.io.br_imm   := (cu_module.io.immediate).asSInt
        // register_module.io.wdata := alu_module.io.out     // PC + 4
        register_module.io.wdata := (pc_module.io.out).asUInt + 4.U
    }.elsewhen(cu_module.io.ins(6,0) === jalr)      // Unconditional branch jalr instructioni
    {
        alu_module.io.alu_Op := 0.U
        alu_module.io.in_A := register_module.io.rdata1
        alu_module.io.in_B := cu_module.io.immediate
        pc_module.io.br_imm := (alu_module.io.out).asSInt      // rs1 + imm

        pc_module.io.jalr_en_pc := cu_module.io.jalr_en
        // alu_module.io.in_A := (pc_module.io.out).asUInt
        // alu_module.io.in_B := 4.U
        alu_module.io.alu_Op := 0.U

        register_module.io.wdata :=  alu_module.io.out
    }


    dmem_module.io.wr_enable := cu_module.io.dmem_en  // Write enable for DMEM
    
    // From register file to DMEM (store instruction)
    cu_module.io.dmem_addr2 := (alu_module.io.out)(1,0)    // 2 Bit address for selecting bytes of memory address
    dmem_module.io.dataIn(0) := 0.U
    dmem_module.io.dataIn(1) := 0.U
    dmem_module.io.dataIn(2) := 0.U
    dmem_module.io.dataIn(3) := 0.U
    // dmem_module.io.dataIn := 0.U
    
    dmem_module.io.mask(0) :=  0.B
    dmem_module.io.mask(1) :=  0.B
    dmem_module.io.mask(2) :=  0.B
    dmem_module.io.mask(3) :=  0.B                               //
    
    // dmem_module.io.mask :=  0.B

    when(cu_module.io.ins(6,0) === "b0100011".U)   //Store Instruction
    {
        alu_module.io.alu_Op := 0.U                     // Calculating address in store instruction
        alu_module.io.in_A := cu_module.io.immediate
        alu_module.io.in_B := register_module.io.rdata1  // write according to S-format
        // val temp_addr   = Cat( Fill( 2,(alu_module.io.out)(31) ), (alu_module.io.out)(31,2) )
        val temp_addr = alu_module.io.out(11,2)

        // val store_address = RegInit(0.U(32.W))
        // store_address := alu_module.io.out(11,2)
        dmem_module.io.addr := alu_module.io.out(11,2)
        dmem_module.io.mask := cu_module.io.mask_val

        // dmem_module.io.mask :=  cu_module.io.mask_val(3) ## cu_module.io.mask_val(2) ## cu_module.io.mask_val(1) ## cu_module.io.mask_val(0)
        
        when(cu_module.io.ins(14,12) === 0.U)             // Func3 
        {
            // when(cu_module.io.dmem_addr2 === 0.U)   // Store Byte
            // {
            //     dmem_module.io.dataIn(0) := register_module.io.rdata2(7,0)
            //     dmem_module.io.dataIn(1) := 0.U
            //     dmem_module.io.dataIn(2) := 0.U
            //     dmem_module.io.dataIn(3) := 0.U
            // }.elsewhen(cu_module.io.dmem_addr2 === 1.U)
            // {
            //     dmem_module.io.dataIn(0) := 0.U
            //     dmem_module.io.dataIn(1) := register_module.io.rdata2(7,0)
            //     dmem_module.io.dataIn(2) := 0.U
            //     dmem_module.io.dataIn(3) := 0.U
            // }.elsewhen(cu_module.io.dmem_addr2 === 2.U)
            // {
            //     dmem_module.io.dataIn(0) := 0.U
            //     dmem_module.io.dataIn(1) := 0.U
            //     dmem_module.io.dataIn(2) := register_module.io.rdata2(7,0)
            //     dmem_module.io.dataIn(3) := 0.U
            // }.elsewhen(cu_module.io.dmem_addr2 === 3.U)
            // {
            //     dmem_module.io.dataIn(0) := 0.U
            //     dmem_module.io.dataIn(1) := 0.U
            //     dmem_module.io.dataIn(2) := 0.U
            //     dmem_module.io.dataIn(3) := register_module.io.rdata2(7,0)
            // }
            dmem_module.io.dataIn(0) := register_module.io.rdata2(7,0)
            dmem_module.io.dataIn(1) := register_module.io.rdata2(7,0)
            dmem_module.io.dataIn(2) := register_module.io.rdata2(7,0)
            dmem_module.io.dataIn(3) := register_module.io.rdata2(7,0)

            // dmem_module.io.dataIn := register_module.io.rdata2(7,0) ## register_module.io.rdata2(7,0) ## register_module.io.rdata2(7,0) ## register_module.io.rdata2(7,0)

        }.elsewhen(cu_module.io.ins(14,12) === 1.U)  // Store Half
        {
            when(cu_module.io.dmem_addr2 === 0.U)
            {
                // As storing a half word requires 2 bytes so we use 2 bytes 
                // for storing in the given address
                dmem_module.io.dataIn(0) := register_module.io.rdata2(7,0)
                dmem_module.io.dataIn(1) := register_module.io.rdata2(15,8)

                // dmem_module.io.dataIn := Fill(16 ,0.U) ## register_module.io.rdata2(15,8) ## register_module.io.rdata2(7,0)  // 15,0

            }.elsewhen(cu_module.io.dmem_addr2 === 1.U)
            {
                // As storing a half word requires 2 bytes so we use 2 bytes 
                // for storing in the given address
                dmem_module.io.dataIn(1) := register_module.io.rdata2(7,0)
                dmem_module.io.dataIn(2) := register_module.io.rdata2(15,8)
                // dmem_module.io.dataIn := Fill(8 ,0.U) ## register_module.io.rdata2(15,8) ## register_module.io.rdata2(7,0) ## Fill(8 ,0.U) // (23,8)
            }.elsewhen(cu_module.io.dmem_addr2 === 2.U)
            {
                // As storing a half word requires 2 bytes so we use 2 bytes 
                // for storing in the given address
                dmem_module.io.dataIn(2) := register_module.io.rdata2(7,0)
                dmem_module.io.dataIn(3) := register_module.io.rdata2(15,8)   

                // dmem_module.io.dataIn := register_module.io.rdata2(15,8) ## register_module.io.rdata2(7,0) ## Fill(16 ,0.U)  //(31,16)
            }.elsewhen(cu_module.io.dmem_addr2 === 3.U)
            {
                // As storing a half word requires 2 bytes so we use 2 bytes 
                // for storing in the given address
                dmem_module.io.dataIn(3) := register_module.io.rdata2(7,0)
                dmem_module.io.addr := temp_addr + 1.U
                dmem_module.io.dataIn(0) := register_module.io.rdata2(15,8)   

                // dmem_module.io.dataIn := register_module.io.rdata2(7,0) ## Fill(24 ,0.U)  // (31, 24)
                // dmem_module.io.addr := temp_addr + 1.U
                // dmem_module.io.dataIn := Fill(24 ,0.U) ## register_module.io.rdata2(15,8)  // (7,0)
                
            }

        }.elsewhen(cu_module.io.ins(14,12) === 2.U)  // Store word
        {
            when(cu_module.io.dmem_addr2 === 0.U)
            {
                // As storing a word requires 4 bytes so we use only first 4 bytes 
                // for storing in any given address
                dmem_module.io.dataIn(0) := register_module.io.rdata2(7,0) 
                dmem_module.io.dataIn(1) := register_module.io.rdata2(15,8)   
                dmem_module.io.dataIn(2) := register_module.io.rdata2(23,16)
                dmem_module.io.dataIn(3) := register_module.io.rdata2(31,24)

                // dmem_module.io.dataIn := register_module.io.rdata2

            }.elsewhen(cu_module.io.dmem_addr2 === 1.U){                        
                // As storing a word requires 4 bytes so we use 4 bytes 
                // for storing in the given address
                dmem_module.io.dataIn(1) := register_module.io.rdata2(7,0) 
                dmem_module.io.dataIn(2) := register_module.io.rdata2(15,8)   
                dmem_module.io.dataIn(3) := register_module.io.rdata2(23,16)
                dmem_module.io.addr := temp_addr + 1.U
                dmem_module.io.dataIn(0) := register_module.io.rdata2(31,24)

                // dmem_module.io.dataIn :=  register_module.io.rdata2(31,8) ## Fill(8, 0.U)   // (31, 8)
                // dmem_module.io.addr := temp_addr + 1.U
                // dmem_module.io.dataIn := Fill(24, 0.U) ## register_module.io.rdata2(31,24)  //(7,0)

            }.elsewhen(cu_module.io.dmem_addr2 === 2.U){                        
                // As storing a word requires 4 bytes so we use 4 bytes 
                // for storing in the given address
                dmem_module.io.dataIn(2) := register_module.io.rdata2(7,0) 
                dmem_module.io.dataIn(3) := register_module.io.rdata2(15,8)   
                dmem_module.io.addr := temp_addr + 1.U
                dmem_module.io.dataIn(0) := register_module.io.rdata2(23,16)
                dmem_module.io.dataIn(1) := register_module.io.rdata2(31,24)

                // dmem_module.io.dataIn := register_module.io.rdata2(15,0) ## Fill(16, 0.U) // (31,16)
                // dmem_module.io.addr := temp_addr + 1.U
                // dmem_module.io.dataIn := Fill(16, 0.U) ## register_module.io.rdata2(31,16)  // (15,0)

            }.elsewhen(cu_module.io.dmem_addr2 === 3.U){                        
                // As storing a word requires 4 bytes so we use 4 bytes 
                // for storing in the given address
                dmem_module.io.dataIn(3) := register_module.io.rdata2(7,0) 
                dmem_module.io.addr := temp_addr + 1.U
                dmem_module.io.dataIn(0) := register_module.io.rdata2(15,8)  
                dmem_module.io.dataIn(1) := register_module.io.rdata2(23,16)
                dmem_module.io.dataIn(2) := register_module.io.rdata2(31,24)

                // dmem_module.io.dataIn := register_module.io.rdata2(7, 0) ## Fill(24, 0.U) // (31,24) Where we are assigning the bits
                // dmem_module.io.addr := temp_addr + 1.U
                // dmem_module.io.dataIn := Fill(8, 0.U) ## register_module.io.rdata2(31,8)
            }

        }


        
        
    }

    // Setting data of register according to func3 of Load instruction
    // From DMEM to register file (load instruction using masking)
    // mistakes: check dmem vector width, address calculation, func3 assignment, dmem_addr2 calculation
    when(cu_module.io.ins(6,0) === 3.U)
    {
        // Using ALU to calculate load address
        alu_module.io.alu_Op := 0.U
        alu_module.io.in_A := cu_module.io.immediate
        alu_module.io.in_B := register_module.io.rdata1
        // val address_load = RegInit(0.U(32.W))
        // address_load := alu_module.io.out(11,2)
        dmem_module.io.addr    := alu_module.io.out(11,2)
        val next_address = alu_module.io.out(11,2)
        
        // dmem_module.io.addr :=  alu_module.io.out(11,2)


        when(cu_module.io.func3_7 === load_B)   // Load Byte
        {// Loading a 8-bit signed num, so while using it, use asSInt
            // When dmem_addr2 is 0 which means our data is in first block of memory(memory has four blocks)
            when(cu_module.io.dmem_addr2 === 0.U)
            {   // used .asSInt because of signed value
                // val lb_num = (dmem_module.io.dataOut(7,0) ).asSInt
                val lb_num = (dmem_module.io.dataOut(0) ).asSInt
                register_module.io.wdata := Cat( Fill(24, lb_num(7) ) , lb_num ).asUInt 

            }.elsewhen(cu_module.io.dmem_addr2 === 1.U)
            {
                // val lb_num = (dmem_module.io.dataOut(15,8) ).asSInt
                val lb_num = (dmem_module.io.dataOut(1) ).asSInt
                register_module.io.wdata := Cat( Fill(24, lb_num(7)) , lb_num ).asUInt

            }.elsewhen(cu_module.io.dmem_addr2 === 2.U)
            {
                // val lb_num = (dmem_module.io.dataOut(23,16) ).asSInt
                val lb_num = (dmem_module.io.dataOut(2) ).asSInt
                register_module.io.wdata := Cat( Fill(24, lb_num(7)) , lb_num ).asUInt

            }.elsewhen(cu_module.io.dmem_addr2 === 3.U)
            {
                // val lb_num = (dmem_module.io.dataOut(31, 24) ).asSInt
                val lb_num = (dmem_module.io.dataOut(3) ).asSInt
                register_module.io.wdata := Cat( Fill(24, lb_num(7)) , lb_num ).asUInt

            }
            
        }
        .elsewhen(cu_module.io.func3_7 === load_H)   // Load Half word
        {// Loading a 16-bit signed num, so while using it, use asSInt
            // When dmem_addr2 is 0 which means our data is in first and second blocks of memory

            when(cu_module.io.dmem_addr2 === 0.U)
            {
                val lh_num = Cat(dmem_module.io.dataOut(1) , dmem_module.io.dataOut(0) ).asSInt
                // val lh_num = Cat(dmem_module.io.dataOut(15,0) ).asSInt
                register_module.io.wdata := Cat( Fill(16, lh_num(15)) , lh_num ).asUInt

            }.elsewhen(cu_module.io.dmem_addr2 === 1.U)
            {
                val lh_num = Cat(dmem_module.io.dataOut(2) , dmem_module.io.dataOut(1) ).asSInt
                // val lh_num = Cat(dmem_module.io.dataOut(23,8) ).asSInt
                register_module.io.wdata := Cat( Fill(16, lh_num(15)) , lh_num ).asUInt
                
            }.elsewhen(cu_module.io.dmem_addr2 === 2.U)
            {
                val lh_num = Cat(dmem_module.io.dataOut(3) , dmem_module.io.dataOut(2) ).asSInt
                // val lh_num = Cat(dmem_module.io.dataOut(31,16) ).asSInt
                register_module.io.wdata := Cat( Fill(16, lh_num(15)) , lh_num ).asUInt
                
            }.elsewhen(cu_module.io.dmem_addr2 === 3.U)
            {
                val lh_num = dmem_module.io.dataOut(3)
                dmem_module.io.addr := next_address + 1.U
                val lh_num2 = Cat( dmem_module.io.dataOut(0), lh_num).asSInt
                register_module.io.wdata := Cat( Fill(16, lh_num2(15)) , lh_num2 ).asUInt

                // val lh_num = dmem_module.io.dataOut(31,24)
                // dmem_module.io.addr := next_address + 1.U
                // val lh_num2 = Cat( dmem_module.io.dataOut(7,0), lh_num).asSInt
                // register_module.io.wdata := Cat( Fill(16, lh_num2(15)) , lh_num2 ).asUInt
                
            }
        }
        .elsewhen(cu_module.io.func3_7 === load_W)
        {// Loading a 32-bit signed num, so while using it, use asSInt
            // For now whatever the address is, we have build the logic that 
            // for every lw bits will be stored from 0th block to 3rd block
            when(cu_module.io.dmem_addr2 === 0.U)
            {
                val lw_num = Cat(dmem_module.io.dataOut(3) , dmem_module.io.dataOut(2),
                dmem_module.io.dataOut(1) , dmem_module.io.dataOut(0) ).asSInt
                register_module.io.wdata := lw_num.asUInt

                // register_module.io.wdata := dmem_module.io.dataOut
            }.elsewhen(cu_module.io.dmem_addr2 === 1.U)
            {
                val lw_num = Cat(dmem_module.io.dataOut(3),dmem_module.io.dataOut(2),dmem_module.io.dataOut(1))
                dmem_module.io.addr := next_address + 1.U
                register_module.io.wdata := Cat( dmem_module.io.dataOut(0) ,lw_num)

                // val lw_num = Cat(dmem_module.io.dataOut(23,8))
                // dmem_module.io.addr := next_address + 1.U
                // register_module.io.wdata := Cat( dmem_module.io.dataOut(7,0) ,lw_num)

            }.elsewhen(cu_module.io.dmem_addr2 === 2.U)
            {
                val lw_num = Cat(dmem_module.io.dataOut(3),dmem_module.io.dataOut(2))
                dmem_module.io.addr := next_address + 1.U
                register_module.io.wdata := Cat(dmem_module.io.dataOut(1), dmem_module.io.dataOut(0) ,lw_num)

                // val lw_num = Cat(dmem_module.io.dataOut(31,16))
                // dmem_module.io.addr := next_address + 1.U
                // register_module.io.wdata := Cat(dmem_module.io.dataOut(15,0),lw_num)
            }.elsewhen(cu_module.io.dmem_addr2 === 3.U)
            {
                val lw_num = dmem_module.io.dataOut(3)
                dmem_module.io.addr := next_address + 1.U
                register_module.io.wdata := Cat(dmem_module.io.dataOut(2), dmem_module.io.dataOut(1), dmem_module.io.dataOut(0), lw_num)

                // val lw_num = dmem_module.io.dataOut(31,24)
                // dmem_module.io.addr := next_address + 1.U
                // register_module.io.wdata := Cat(dmem_module.io.dataOut(23,0), lw_num)
            }


        }
        .elsewhen(cu_module.io.func3_7 === load_BU)
        {
            when(cu_module.io.dmem_addr2 === 0.U)
            {   
                val lbu_num = (dmem_module.io.dataOut(0) )
                register_module.io.wdata := Cat( Fill(24, lbu_num(7)) , lbu_num ).asUInt

                // val lbu_num = (dmem_module.io.dataOut(7,0) )
                // register_module.io.wdata := Cat( Fill(24, lbu_num(7) ) , lbu_num ).asUInt 

            }.elsewhen(cu_module.io.dmem_addr2 === 1.U)
            {
                val lbu_num = (dmem_module.io.dataOut(1) )
                register_module.io.wdata := Cat( Fill(24, lbu_num(7)) , lbu_num ).asUInt

                // val lb_num = (dmem_module.io.dataOut(15,8) )
                // register_module.io.wdata := Cat( Fill(24, lb_num(7)) , lb_num ).asUInt

            }.elsewhen(cu_module.io.dmem_addr2 === 2.U)
            {
                val lbu_num = (dmem_module.io.dataOut(2) )
                register_module.io.wdata := Cat( Fill(24, lbu_num(7)) , lbu_num ).asUInt

                // val lb_num = (dmem_module.io.dataOut(23,16) )
                // register_module.io.wdata := Cat( Fill(24, lb_num(7)) , lb_num ).asUInt

            }.elsewhen(cu_module.io.dmem_addr2 === 3.U)
            {
                val lbu_num = (dmem_module.io.dataOut(3) )
                register_module.io.wdata := Cat( Fill(24, lbu_num(7)) , lbu_num ).asUInt

                // val lb_num = (dmem_module.io.dataOut(31, 24) )
                // register_module.io.wdata := Cat( Fill(24, lb_num(7)) , lb_num ).asUInt

            }

            // val lbu_num = dmem_module.io.dataOut(0)
            // register_module.io.wdata := lbu_num.asUInt
        }
        .elsewhen(cu_module.io.func3_7 === load_HU)
        {
            when(cu_module.io.dmem_addr2 === 0.U)
            {
                val lhu_num = Cat(dmem_module.io.dataOut(1) , dmem_module.io.dataOut(0) )
                register_module.io.wdata := Cat( Fill(16, lhu_num(15)) , lhu_num ).asUInt

                // val lh_num = Cat(dmem_module.io.dataOut(15,0) )
                // register_module.io.wdata := Cat( Fill(16, lh_num(15)) , lh_num ).asUInt

            }.elsewhen(cu_module.io.dmem_addr2 === 1.U)
            {
                val lhu_num = Cat(dmem_module.io.dataOut(2) , dmem_module.io.dataOut(1) )
                register_module.io.wdata := Cat( Fill(16, lhu_num(15)) , lhu_num ).asUInt

                // val lh_num = Cat(dmem_module.io.dataOut(23,8) ).asSInt
                // register_module.io.wdata := Cat( Fill(16, lh_num(15)) , lh_num ).asUInt
                
            }.elsewhen(cu_module.io.dmem_addr2 === 2.U)
            {
                val lhu_num = Cat(dmem_module.io.dataOut(3) , dmem_module.io.dataOut(2) )
                register_module.io.wdata := Cat( Fill(16, lhu_num(15)) , lhu_num ).asUInt

                // val lh_num = Cat(dmem_module.io.dataOut(31,16) ).asSInt
                // register_module.io.wdata := Cat( Fill(16, lh_num(15)) , lh_num ).asUInt
                
            }.elsewhen(cu_module.io.dmem_addr2 === 3.U)
            {
                val lh_num = dmem_module.io.dataOut(3)
                dmem_module.io.addr := next_address + 1.U
                val lh_num2 = Cat( dmem_module.io.dataOut(0), lh_num)
                register_module.io.wdata := Cat( Fill(16, lh_num2(15)) , lh_num2 ).asUInt

                // val lh_num = dmem_module.io.dataOut(31,24)
                // dmem_module.io.addr := next_address + 1.U
                // val lh_num2 = Cat( dmem_module.io.dataOut(7,0), lh_num).asSInt
                // register_module.io.wdata := Cat( Fill(16, lh_num2(15)) , lh_num2 ).asUInt
                
            }


        }

        
        
    }     // Load intruction using masking finished
    
    io.out := (register_module.io.wdata).asSInt   // changing the output to signed
    
    // when(cu_module.io.ins(6,0) === "h23".U)
    // {
    //     io.out := (dmem_module.io.dataIn(1)).asSInt
    // }
    // io.out := (pc_module.io.br_imm).asSInt
    
   
}