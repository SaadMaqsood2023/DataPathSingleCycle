package DataPathSSP

import chisel3._
import chisel3.util._
// import chisel3.util.experimental.loadMemoryFromFile

object SSP_size_op
{
    val reg_add_len = 5
    val mem_width = 32
    val imm_len = 32  // extended 
    val fn3_7   = 4
    val R_type  = "h33".U
    val I_type  = "h13".U
    val L_type  = "h3".U
    val S_type  = "h23".U
    val Lui_ins = "h37".U
    val AUIpc_ins= "h17".U
    val B_type  = "h63".U
    val UJ_type = "h6f".U
    val JalR_type = "h67".U
}

import SSP_size_op._
class CU extends Module
{
    val io = IO(new Bundle {
    
    // val ins = Input( UInt( mem_len.W ) )
    val ins     = Input(UInt( mem_width.W ) )
    val dmem_addr2 = Input(UInt(2.W))
    val w_en    = Output( Bool() )
    val imem_en = Output( Bool() )
    val rs1     = Output(UInt(reg_add_len.W) )
    val rs2     = Output(UInt(reg_add_len.W) )
    val rd      = Output(UInt(reg_add_len.W) )
    val r_en    = Output(Bool() )
    val dmem_en = Output(Bool() )
    val immediate = Output(UInt(imm_len.W))
    val func3_7 = Output(UInt(fn3_7.W))
    val br_en   = Output(Bool())
    val unbr_en   = Output(Bool())
    val mask_val  = Output(Vec(4, Bool() ) )   //
    val jalr_en = Output( Bool() )
    })

    ///
    io.imem_en := 0.B// Value for imem enable

    io.w_en := 0.B   // Default value for register file enable

    io.br_en := 0.B  // Default value for branch enable of branch module

    io.unbr_en := 0.B  // Default value for unconditional branch module

    io.jalr_en := 0.B

    io.mask_val(0) := 0.B   // Default value for mask vector     //
    io.mask_val(1) := 0.B
    io.mask_val(2) := 0.B
    io.mask_val(3) := 0.B
    

    io.rs1 := io.ins(19, 15)
    io.rs2 := io.ins(24, 20)
    io.rd := io.ins(11, 7)
    io.immediate := 0.U      // immediate of instruction
    // io.func3_7 :=  Cat(io.ins(30), io.ins(14,12))
    // Setting register file write enable and func3 and func7 in R-Type
    // and only func3 in other types
    io.func3_7 :=  io.ins(14, 12)
    // io.input_dmem(0) := 0.U 
    // io.input_dmem(1) := 0.U 
    // io.input_dmem(2) := 0.U 
    // io.input_dmem(3) := 0.U

    val func3_sll = 1.U
    val func3_srl = 5.U
    val func3_sra = 13.U

    io.dmem_en := 0.B  // Default value for DMEM write enable
    io.r_en := 0.B     // Default value for DMEM read enable
    when(io.ins(6,0) === R_type  )
    {
        io.w_en := 1.B
        io.func3_7 :=  Cat(io.ins(30), io.ins(14,12))
    }.elsewhen(io.ins(6,0) === I_type )
    {
        io.w_en := 1.B
        io.immediate := Cat( Fill( 20, io.ins(31) ), io.ins(31, 20) )   // Extending last bit to preserve sign
        // Cannot write this condition with OR in the above condition
        when(io.ins(14,12) === func3_sll
        || io.ins(14,12) === func3_srl || io.ins(14,12) === func3_sra)
        {
            io.func3_7 :=  Cat(io.ins(30), io.ins(14,12))
        }
    }.elsewhen(io.ins(6,0) === L_type)
    {
        io.immediate := Cat( Fill( 20, io.ins(31) ), io.ins(31, 20) )   // Extending last bit to preserve sign
        io.w_en := 1.B
        // io.func3_7 := 0.U
        io.r_en := 1.B
    }.elsewhen(io.ins(6,0) === S_type)
    {
        val imm_temp = Cat( io.ins(31,25), io.ins(11,7) )               // Extending last bit to preserve sign
        io.immediate := Cat(Fill(20, imm_temp(11)), imm_temp) 
        io.w_en := 0.B
        // io.func3_7 := 0.U
        io.r_en := 1.B
        // dmem write enable
        io.dmem_en := 1.B

        when(io.ins(14,12) === 0.U)     // Store Byte in CU
        {
            when(io.dmem_addr2 === 0.U)   
            {
                io.mask_val(0) := 1.B   // value for mask vector according to address
                io.mask_val(1) := 0.B
                io.mask_val(2) := 0.B
                io.mask_val(3) := 0.B
            }.elsewhen(io.dmem_addr2 === 1.U)
            {
                io.mask_val(0) := 0.B   // value for mask vector according to address
                io.mask_val(1) := 1.B
                io.mask_val(2) := 0.B
                io.mask_val(3) := 0.B
            }.elsewhen(io.dmem_addr2 === 2.U)
            {
                io.mask_val(0) := 0.B   // value for mask vector according to address
                io.mask_val(1) := 0.B
                io.mask_val(2) := 1.B
                io.mask_val(3) := 0.B
            }.elsewhen(io.dmem_addr2 === 3.U)
            {
                io.mask_val(0) := 0.B   // value for mask vector according to address
                io.mask_val(1) := 0.B
                io.mask_val(2) := 0.B
                io.mask_val(3) := 1.B
            }

        }.elsewhen(io.ins(14,12) === 1.U)  // Store Half in CU
        {
            when(io.dmem_addr2 === 0.U)
            {
                io.mask_val(0) := 1.B   // value for mask vector according to address
                io.mask_val(1) := 1.B
                io.mask_val(2) := 0.B
                io.mask_val(3) := 0.B
            }.elsewhen(io.dmem_addr2 === 1.U)
            {
                io.mask_val(0) := 0.B   // value for mask vector according to address
                io.mask_val(1) := 1.B
                io.mask_val(2) := 1.B
                io.mask_val(3) := 0.B
            }.elsewhen(io.dmem_addr2 === 2.U)
            {
                io.mask_val(0) := 0.B   // value for mask vector according to address
                io.mask_val(1) := 0.B
                io.mask_val(2) := 1.B
                io.mask_val(3) := 1.B
            }.elsewhen(io.dmem_addr2 === 3.U)
            {
                io.mask_val(0) := 1.B   // using interrupt as storing half word requires two bytes
                io.mask_val(1) := 0.B
                io.mask_val(2) := 0.B
                io.mask_val(3) := 1.B
            }

        }.elsewhen(io.ins(14,12) === 2.U)  // Store word in CU
        {
            when(io.dmem_addr2 === 0.U)
            {
                io.mask_val(0) := 1.B   // value for mask vector according to address
                io.mask_val(1) := 1.B
                io.mask_val(2) := 1.B
                io.mask_val(3) := 1.B
            }.otherwise{
                io.mask_val(0) := 1.B   // As storing a word requires 4 bytes so we use only first 4 bytes 
                io.mask_val(1) := 1.B   // for storing in any given address
                io.mask_val(2) := 1.B
                io.mask_val(3) := 1.B
            }
        }



    }.elsewhen(io.ins(6,0) === Lui_ins)
    {
        io.immediate := Cat( io.ins(31, 12), Fill(12, 0.U) )  // can also use left shift for upper immediate
        io.w_en := 1.B
        io.func3_7 := 0.U
    }.elsewhen(io.ins(6,0) === AUIpc_ins)
    {
        io.immediate := ( io.ins(31,12) ) << 12
        io.w_en := 1.B
        io.func3_7 := 0.U
    }.elsewhen(io.ins(6,0) === B_type)
    {
        // imm[12|10:5]  rs2  rs1  func3  imm[4:1|11]  opcode
        val temp = Cat(io.ins(31), io.ins(7), io.ins(30,25),io.ins(11,8), 0.U)
        io.immediate := Cat( Fill(19, temp(12)), temp )   // Branch immediate
        io.w_en := 0.B             // not enabling register write in Branch Type
        io.func3_7 := io.ins(14,12)
        io.br_en := 1.B
    }.elsewhen(io.ins(6,0) === UJ_type)   // jal instruction
    {
        // imm[20|10:1|11|19:12]  rd  opcode
        val temp = Cat( io.ins(31),io.ins(19,12),io.ins(20),io.ins(30,21), 0.U )
        io.immediate := Cat( Fill(11, temp(20) ), temp )
        io.w_en := 1.B
        io.func3_7  := 0.U
        io.unbr_en  := 1.B
    }.elsewhen(io.ins(6,0) === JalR_type)
    {
        io.immediate := Cat( Fill( 20, io.ins(31) ), io.ins(31, 20) )
        io.w_en := 1.B
        io.func3_7 := 0.U
        io.jalr_en := 1.B
    }
    

}
