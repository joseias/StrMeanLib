package strmean.data;

public final class Operation {

    public char type = ' ';	  //s-ustitución, b-borrado, i-inserción
    public char a = ' ', b = ' '; //characteres del alfabeto
    public int posSource = -1;    //source string position
    public int posTarget;         //target string position
    public String opS;            //string representing the operation, not consistent with toString...
    public OpInfo opInfo;

    public Operation(char op, char a, char b, int posS, int posT, OpInfo opInfo) 
    {
        this.type = op;
        this.a = a;
        this.b = b;
        this.posSource = posS;
        this.posTarget=posT;
        this.opInfo=opInfo;

        opS="("+Character.toString(op)+":"+Character.toString(a)+":"+Character.toString(b)+":"+Integer.toString(posS)+")";
    }

    @Override
    public String toString() {
        String s = "" + type + "-";
        if (type == 's' || type=='w') {
            s += "" + a + "-" + b;
        }
        else {
            s += "" + a + "-" + "-";
        }
        s += "-posS:" + posSource + "-posT:"+ posTarget +"-quality:" + this.opInfo.quality;
        //s+="\n";
        return s;
    }

    @Override
    public int hashCode()
    {
        return this.opS.hashCode();
    }
    
    @Override
    public boolean equals(Object o)
    {

        if(!(o instanceof  Operation))
        {
            return false;
        }
        else
        {
            Operation ob=(Operation)o;
            return (this.type==ob.type && this.posSource==ob.posSource && this.a==ob.a && this.b==ob.b);
        }
    }
}
