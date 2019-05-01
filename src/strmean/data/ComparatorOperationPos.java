/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package strmean.data;

import java.util.Comparator;

/**
 *
 * @author fj
 */
public class ComparatorOperationPos  implements Comparator<Operation>
{
    @Override
    public int compare(Operation a, Operation b)
    {
        int cmp=(int)Math.signum(a.posSource-b.posSource);
        if(cmp==0)
        {
            return (int)Math.signum(a.posTarget-b.posTarget);
        
        }
        else
        {
            return cmp;
        }
    }
    
    public static void main(String[] args)
    {
//        Operation op1=new Operation('i', 'a', 'b', 0, 2, 2,0,0);
//        Operation op2=new Operation('i', 'a', 'b', 1, 3, 2,0,0);
//        ComparatorOperationPos cop=new ComparatorOperationPos();
//        System.out.println(cop.compare(op1, op2));
    }
}
