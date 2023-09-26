package DataPathSSP

import chisel3._
import chisel3.util._
import chisel3.util.experimental.loadMemoryFromFile

object SSP_size
{
    val mem_len = 256
    val mem_width = 32
    val div_4 = 2
}

import SSP_size._
// class IMEM(file : String) extends Module
class IMEM extends Module
{
    val io = IO(new Bundle {
    
    val address = Input( UInt( mem_width.W ) )
    val dataIn = Input(UInt( mem_width.W ) )
    val wen = Input(Bool())
    val dataOut = Output(UInt(mem_width.W))

    })

    // io.wen := false.B

    // io.dataOut := 0.U
    // Async memory
    val async_mem = Mem(256, UInt(mem_width.W))
    // val async_mem = SyncReadMem(256, UInt(mem_width.W))

    when(io.wen)
    {
        async_mem.write(io.address, io.dataIn)
    }
    // val address_pc = RegInit(0.U(32.W)) 
    val address_pc = Fill(2, 0.U) ## io.address >> div_4
    // loadMemoryFromFile(async_mem, "./Scala-Chisel-Learning-Journey-main/src/main/scala/DataPathSSP/assemblyHexcode.txt")
    // write the full path
    loadMemoryFromFile(async_mem, "/home/saad/Documents/Scala-Chisel-Learning-Journey/src/main/scala/gcd/DataPathSSP/assemblyHexcode.txt")
    io.dataOut := async_mem.read(address_pc)
}
