package DataPathSSP

import chisel3._
import chisel3.util._

object Branch_fnc
{
    val fn_size = 3
    val input_size = 32
    val beq = 0.U
    val bne = 1.U
    val blt = 4.U
    val bge = 5.U
    val bltu = 6.U
    val bgeu = 7.U
}

import Branch_fnc._

class Branch extends Module
{
    val io = IO(new Bundle {
        val func3 = Input( UInt(fn_size.W) )
        val br_ins = Input( Bool() )
        val inpA  = Input( UInt(input_size.W) )
        val inpB  = Input( UInt(input_size.W) )
        val br_taken = Output( Bool() )
    })

    io.br_taken := 0.B
    when(io.br_ins)
    {
        switch(io.func3)
        {
            is(beq)
            {
                when(io.inpA === io.inpB)
                {
                    io.br_taken := 1.B
                }
            }
            is(bne)
            {
                when(io.inpA =/= io.inpB)
                {
                    io.br_taken := 1.B
                }
            }
            is(blt)
            {
                when(io.inpA <= io.inpB)
                {
                    io.br_taken := 1.B
                }
            }
            is(bge)
            {
                when(io.inpA >= io.inpB)
                {
                    io.br_taken := 1.B
                }
            }
            is(bltu)
            {
                when( (io.inpA).asUInt <= (io.inpB).asUInt)
                {
                    io.br_taken := 1.B
                }
            }
            is(bgeu)
            {
                when( (io.inpA).asUInt >= (io.inpB).asUInt)
                {
                    io.br_taken := 1.B
                }
            }

        }

    }
    
}