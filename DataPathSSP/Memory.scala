// package DataPathSSP

// import chisel3._
// import chisel3.util._

// class Memory extends Module 
// {
//     val io = IO( new Bundle {
//         val dataIn = Input( UInt(32.W))
//         val wen = Input(Bool())
//         val ren = Input(Bool())
//         val rd_addr = Input(UInt(32.W))
//         val wr_addr = Input(UInt(32.W))
//         val dataOut = Output(UInt(32.W))
//     })

//     io.dataOut := 0.U

//     val mem = Mem(1024, Vec(4, UInt(8.W) ) )

//     when(io.wen)
//     {
//         mem.write(io.address, io.dataIn)
//     }
// }