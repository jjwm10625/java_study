fun int power(int x, int n)
    if (n==0) then return 1;
    else return x*power(n-1, n-2);

int a=2; 
int b=10;
print power(a,b);
print power(b,a);
