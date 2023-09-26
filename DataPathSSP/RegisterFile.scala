package DataPathSSP

import chisel3 . _
import chisel3.util._

object FileSize
{
    val XLEN = 32
    val REGFILE_LEN = 32

}
import FileSize._

class RegFileIO extends Bundle {
    val raddr1 = Input ( UInt (5. W ) )  // Address of source registers 
    val raddr2 = Input ( UInt (5. W ) )

    val rdata1 = Output ( UInt ( XLEN . W ) )  // Ouput of reading data
    val rdata2 = Output ( UInt ( XLEN . W ) )

    val wen = Input ( Bool () )     // write enable pin
    val waddr = Input ( UInt (5.W ) )   // address of destination register
    val wdata = Input ( UInt ( XLEN . W ) )  // data to be written in destination register

}
class RegisterFile extends Module 
{
    val io = IO (new RegFileIO )
    
    val regs = Reg ( Vec ( REGFILE_LEN , UInt ( XLEN . W ) ) )
    io.rdata1 := Mux (( io.raddr1.orR ) , regs ( io.raddr1 ) , 0.U )  // Using OR reduction to calculate x0

    io.rdata2 := Mux (( io.raddr2.orR ) , regs ( io.raddr2 ) , 0. U )
    
    when ( io.wen & io.waddr.orR ) {  // can write in all registers except x0
        regs ( io.waddr ) := io.wdata  // writing data
        io.rdata1 := Mux (( io.raddr1.orR ) , regs ( io.raddr1 ) , 0.U )  // Using OR reduction to calculate x0

        io.rdata2 := Mux (( io.raddr2.orR ) , regs ( io.raddr2 ) , 0.U )
    }

}

