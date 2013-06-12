package ro.igstan.lixp

object stdlib {
  val add = NativeDefValue(args => args(0) + args(1))
  val sub = NativeDefValue(args => args(0) - args(1))
  val mul = NativeDefValue(args => args(0) * args(1))
  val div = NativeDefValue(args => args(0) / args(1))
}
