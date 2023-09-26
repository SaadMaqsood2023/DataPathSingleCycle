package DataPathSSP

// Optimized counter example
import chisel3._
import chisel3.util._
class PC (val max : Int , val min : Int = 0) extends Module {

    val io = IO (new Bundle {
        val en_jump = Input( Bool() )
        val br_imm  = Input( SInt(32.W))
        val jalr_en_pc = Input( Bool() )
        val out = Output ( SInt ( log2Ceil ( max ).W ) )
    })
    val counter = RegInit ( min . S ( log2Ceil ( max ) . W ) )
    
    // with the below condition 
    val count_buffer = Mux(io.jalr_en_pc, io.br_imm, Mux( io.en_jump , io.br_imm, Mux( counter === max.S ,
                             min.S , counter + 4.S ) ) )
    
    counter := count_buffer

    io.out := counter

}