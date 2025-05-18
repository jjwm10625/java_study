int i = 1;

fun int f(int x) {
     i = i + x; 
     return i;
}

fun int g(int x) {
     i = i + x*x;
     return i;
}

let
    int i=10; 
in 
    print f(1);
    print i;
    print g(2);
    print i;
end;
