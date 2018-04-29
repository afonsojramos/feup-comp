.class public all
.super java/lang/Object

.field static a I
.field static b I = 1
.method public static funcAbove(II)I
.limit stack 10
.limit locals 10
ldc "arg1 = "
iload_0
invokestatic io/println(Ljava/lang/String;I)V
ldc "arg2 = "
iload_1
invokestatic io/println(Ljava/lang/String;I)V

iload_0
iload_1
iadd
istore_2

ldc "ret in funcAbove = "
iload_2
invokestatic io/println(Ljava/lang/String;I)V
iload_2
ireturn
.end method
.method public static main([Ljava/lang/String;)V
.limit stack 10
.limit locals 10
ldc "a = "
getstatic all/a I
invokestatic io/println(Ljava/lang/String;I)V
ldc "b = "
getstatic all/b I
invokestatic io/println(Ljava/lang/String;I)V
ldc "1 = "
iconst_1
invokestatic io/println(Ljava/lang/String;I)V

getstatic all/a I
getstatic all/b I
iadd
istore 6


getstatic all/a I
getstatic all/b I
isub
istore_0


getstatic all/a I
getstatic all/b I
imul
istore_3


getstatic all/a I
getstatic all/b I
idiv
istore_1

ldc "sum = "
iload 6
invokestatic io/println(Ljava/lang/String;I)V
ldc "dif = "
iload_0
invokestatic io/println(Ljava/lang/String;I)V
ldc "mul = "
iload_3
invokestatic io/println(Ljava/lang/String;I)V
ldc "div = "
iload_1
invokestatic io/println(Ljava/lang/String;I)V
invokestatic io/println()V

getstatic all/a I
getstatic all/b I
iadd
istore_2

ldc "c = "
iload_2
invokestatic io/println(Ljava/lang/String;I)V
invokestatic io/println()V

getstatic all/b I
iload_2
invokestatic all/funcAbove(II)I
istore 5


getstatic all/b I
iload_2
invokestatic all/funcBelow(II)I
istore 4

ldc "funcAbove of b c = "
iload 5
invokestatic io/println(Ljava/lang/String;I)V
ldc "funcBelow of b c = "
iload 4
invokestatic io/println(Ljava/lang/String;I)V
return
.end method
.method public static funcBelow(II)I
.limit stack 10
.limit locals 10
ldc "arg1 = "
iload_0
invokestatic io/println(Ljava/lang/String;I)V
ldc "arg2 = "
iload_1
invokestatic io/println(Ljava/lang/String;I)V

iload_0
iload_1
iadd
istore_2

ldc "ret in funcBelow = "
iload_2
invokestatic io/println(Ljava/lang/String;I)V
iload_2
ireturn
.end method