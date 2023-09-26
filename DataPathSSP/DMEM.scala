package DataPathSSP

// parameterized memory
import chisel3 . _
import chisel3 . util . _

class DMEM (val size : Int = 1024 , val width : Int = 32) extends Module {
    
    val io = IO (new Bundle {
    // val dataIn = Input ( UInt ( width . W ) )
    // val dataOut = Output ( UInt ( width . W ) )
    val addr = Input ( UInt ( log2Ceil ( size ) . W ) )
    val rd_enable = Input ( Bool () )
    val wr_enable = Input ( Bool () )
    val mask   = Input(Vec(4, Bool()) )
    // val mask = Input(UInt(4.W))
    val dataIn = Input(Vec(4, UInt(8.W)) )
    val dataOut = Output(Vec(4, UInt(8.W)) )
    })
    // val Sync_memory = SyncReadMem( size , Vec( 4, UInt( 8.W ) ) )  // initialize memory with vector when using vectors
    val Sync_memory = Mem( size , Vec( 4, UInt( 8.W ) ) )

    // memory write operation
    when ( io.wr_enable ) {
        // Sync_memory.write ( io.addr , io.dataIn)
        Sync_memory.write ( io.addr , io.dataIn, io.mask )
    }
    // io.dataOut := (Sync_memory.read ( io.addr , io.rd_enable ) )
    io . dataOut := Sync_memory.read ( io.addr )

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
    // io.dataOut := (Sync_memory.read ( io.addr , io.rd_enable ) )
    // // io . dataOut := Sync_memory.read ( io.addr )

    

    // // val Sync_memory = SyncReadMem (1024 , Vec(4, UInt (8.W)))
    // val Sync_memory = Mem (1024 , Vec(4, UInt (8.W)))

    // // val rdVec = Sync_memory.read(io.addr, io.rd_enable)
    // val rdVec = Sync_memory.read(io.addr)
    
    // val wrVec = Wire(Vec(4, UInt (8.W)))
    // val wrMask = Wire(Vec(4, Bool ()))

    // for (i <- 0 until 4) {
    //     wrVec(i) := io.dataIn(i * 8 + 7, i * 8)
    //     wrMask(i) := io.mask(i)
    // }
    // // val temp_write = Reg(UInt(32.W))
    // // temp_write := 
    // val temp_write = RegNext(wrVec(3) ## wrVec(2) ## wrVec(1) ## wrVec(0) )
    // when (io.wr_enable) {
    //     Sync_memory.write(io.addr , wrVec , wrMask)
    // }


    // io.dataOut :=Mux(io.wr_enable, temp_write, rdVec(3) ## rdVec(2) ## rdVec(1) ## rdVec(0) )
    // // io.dataOut := rdVec
}