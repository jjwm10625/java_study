fun int s(int n, int m)
   if (n==0) then return m;
   else return m + s(n-1, n+1);

let int a=10;
in
   print s(a, 0);
   print s(a, 1);
   print s(a, 2);
   print s(a, 3);
end;
