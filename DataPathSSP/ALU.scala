package DataPathSSP

import chisel3 . _
import chisel3 . util . _
// import chiseltest._

// use variables for comparing the func3 or func7, so that you can remember that for what purpose you were using the opcode
// secondly it is good coding practice, which makes it easy for others to read the code 

object ALUOP {
    // ALU Operations , may expand / modify in future
    val ALU_ADD = 0. U (4. W )
    val ALU_SUB = 8. U (4. W )
    val ALU_SLL = 1. U (4. W )
    val ALU_SLT = 2. U (4. W )
    val ALU_SLTU = 3. U (4. W )
    val ALU_XOR = 4. U (4. W )
    val ALU_SRL = 5. U (4. W )
    val ALU_SRA = 13. U (4. W )
    val ALU_OR = 6. U (4. W )
    val ALU_AND = 7. U (4. W )
    
    
    val ALU_COPY_A = 10. U (4. W )
    val ALU_COPY_B = 11. U (4. W )
    // val ALU_XXX = 15. U (4. W )
}

trait Config {
    // word length configuration parameter
    val WLEN = 32
    // ALU operation control signal width
    val ALUOP_SIG_LEN = 4
}


import ALUOP . _
class ALUIO extends Bundle with Config {
    val in_A = Input ( UInt ( WLEN . W ) )
    val in_B = Input ( UInt ( WLEN . W ) )
    val alu_Op = Input ( UInt ( ALUOP_SIG_LEN . W ) )
    val out = Output ( UInt ( WLEN . W ) )
    val sum = Output ( UInt ( WLEN . W ) )
}
class ALU extends Module with Config {
    val io = IO (new ALUIO )
    val sum = (io.in_A) + Mux( io.alu_Op (3) , (-io.in_B) , (io.in_B) )

    val cmp = Mux( io.in_A ( WLEN -1) === io.in_B ( WLEN -1) , sum ( WLEN -1) ,
    Mux( io . alu_Op (1) , io.in_B ( WLEN - 1 ) , io.in_A ( WLEN - 1) ) )

    val shamt = io.in_B (4 ,0).asUInt

    // In the previous alu SRA was at 8 But in Risc-v SRl has 5 func3
    val shin = Mux( io . alu_Op (3) || io.alu_Op === 5.U , io . in_A , Reverse ( io.in_A ) )

    val shiftr = ( Cat ( io.alu_Op (0) && shin ( WLEN - 1) , shin ).asSInt >> shamt ) (    // Shift right arithmetic 
    WLEN -1 , 0)

    val shiftl = Reverse ( shiftr )


    io.out := 0.U
    io.sum := 0.U
    switch(io.alu_Op)
    {
        is(ALU_ADD)
        {
            io.out := sum
            // io.sum := sum
        }
        is(ALU_SUB)
        {
            io.out := sum
            // io.sum := sum
        }
        is(ALU_SLT)
        {
            io.out := (cmp)
            // io.sum := sum
        }
        is(ALU_SLTU)
        {
            io.out := (cmp)
            // io.sum := sum
        }
        is(ALU_SRA)
        {
            io.out := (shiftr).asUInt
            // io.sum := sum
        }
        is(ALU_SRL)
        {
            io.out := shiftr
            // io.sum := sum
        }
        is(ALU_SLL)
        {
            io.out := (shiftl)
            // io.sum := sum
        }
        is(ALU_AND)
        {
            io.out := (io.in_A & io.in_B) 
            // io.sum := sum
        }
        is(ALU_OR)
        {
            io.out := (io.in_A | io.in_B)
            // io.sum := sum
        }
        is(ALU_XOR)
        {
            io.out := (io.in_A ^ io.in_B)
            // io.sum := sum
        }
        is(ALU_COPY_A)
        {
            io.out := io.in_A
            // io.sum := sum
        }
        is(ALU_COPY_B)
        {
            io.out := io.in_B
            // io.sum := sum
        }


    }

    //mistakes are 
    // XLEN to be converted to WLEN, exclude space from < <, remove .U from all the ALUOP variables 


    // val out =
    // Mux( io . alu_Op === ALU_ADD  || io . alu_Op === ALU_SUB , sum ,
    // Mux( io . alu_Op === ALU_SLT  || io . alu_Op === ALU_SLTU , cmp ,
    // Mux( io . alu_Op === ALU_SRA  || io . alu_Op === ALU_SRL , shiftr ,
    // Mux( io . alu_Op === ALU_SLL , shiftl ,
    // Mux( io . alu_Op === ALU_AND  , ( io . in_A & io . in_B ) ,
    // Mux( io . alu_Op === ALU_OR  , ( io . in_A | io . in_B ) ,
    // Mux( io . alu_Op === ALU_XOR  , ( io . in_A ^ io . in_B ) ,
    // Mux( io . alu_Op === ALU_COPY_A  , io . in_A ,
    // Mux( io . alu_Op === ALU_COPY_A  , io . in_B , 0. U ) ) ) ) ) ) ) ) )
    // io . out := out
    // io . sum := sum
}

