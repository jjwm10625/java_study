fun int s(int n, int m)
   if (m==0) then return n;
   else return s(n-1, n-2) + m;

let int a =10;
in
   print s(a, 0);
   print s(a, 1);
   print s(a, 2);
   print s(a, 3);
end;
