package ro.igstan.lixp

object stdlib {
  val add = NativeDefValue { args =>
    val a = args.map {
      case NumValue(v) => v
      case _           => sys.error("addition only works with numbers")
    }

    Right(NumValue(a(0) + a(1)))
  }

  val sub = NativeDefValue { args =>
    val a = args.map {
      case NumValue(v) => v
      case _           => sys.error("subtraction only works with numbers")
    }

    Right(NumValue(a(0) - a(1)))
  }

  val mul = NativeDefValue { args =>
    val a = args.map {
      case NumValue(v) => v
      case _           => sys.error("multiplication only works with numbers")
    }

    Right(NumValue(a(0) * a(1)))
  }

  val div = NativeDefValue { args =>
    val a = args.map {
      case NumValue(v) => v
      case _           => sys.error("division only works with numbers")
    }

    Right(NumValue(a(0) / a(1)))
  }

  val print = NativeDefValue { args =>
    args.map {
      case NumValue(n)           => println(n)
      case d @ DefValue(_, _, _) => println("<fn@%x>".format(d.hashCode))
      case d @ NativeDefValue(_) => println("<fn@%x>".format(d.hashCode))
    }
    Right(NumValue(0))
  }
}
