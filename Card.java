/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package texasholdem;

/**
 *
 * @author Danny
 * Date: //
 * This work was completly my own.
 */
class Card implements Comparable<Card>
{
    
    
   int value;
   Suit suit;
   
   
   public int compareTo(Card o)
    {
        if(value<o.value) return -1;
        if(value==o.value) return 0;
        
        return 1;
    }

   public Card(int v, Suit s)
   {
       value = v;
       suit  = s;
   }

    @Override
   public String toString()
   {
       String out = suit.toString() + " ";
       
       if(value==14) out+="A";
       else if(value==13) out+="K";
       else if(value==12) out+="Q";
       else if(value==11) out+="J";
       else out+=value;
       
       
       
       return out;
   }
}