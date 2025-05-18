fun int fibo(int n)
   if (n <= 2) then return 1;
   else return fibo(n-1) + fibo(n-2);

let int a = 10;
in
   print fibo(a);
   print fibo(a+a);
end;
