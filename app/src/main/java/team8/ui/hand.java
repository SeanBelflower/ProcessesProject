package team8.ui;/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */



import java.util.Arrays;


public class hand {
    public static int score(Card[] hand)
    {
        int handStrength=0;
        
        boolean straight = false;
        boolean flush    = false;
        boolean pair     = false;
        boolean pair2    = false;
        boolean three    = false;
        boolean four     = false;
        
        int[] count = new int[15];
        
        Arrays.sort(hand);
        
        for(int i=0; i<5; i++) count[hand[i].value]++;
        
        for(int i=2; i<15; i++)
        {
            if(pair && count[i]==2) pair2 = true;
            else if(count[i]==2) pair = true;
            else if(count[i]==3) three = true;
            else if(count[i]==4) four = true;
        }
        
        
        if(pair2)
        {
            handStrength += 3 * 0x100000;
            
            boolean bigger = false;
            for(int i=14; i>1; i--)
            {
                if(count[i]==2 && !bigger)
                {
                    bigger = true;
                    handStrength += i * 0x011000;
                    
                } else if(count[i]==2) handStrength += i * 0x000110;
                else if(count[i]==1) handStrength += i;
            }
            
            return handStrength;
            
        } else if(pair && three)
        {
            handStrength += 7 * 0x100000;
            
            for(int i=14; i>1; i--)
            {
                if(count[i]==3) handStrength += i * 0x011100;
                else if(count[i]==2) handStrength += i * 0x000011;
            }
            
            return handStrength;
            
        } else if(four)
        {
            handStrength += 8 * 0x100000;
            
            for(int i=14; i>1; i--)
            {
                if(count[i]==4) handStrength += i * 0x011110;
                else if(count[i]==1) handStrength += i;
            }
            
            return handStrength;
            
        } else if(three)
        {
            handStrength += 4 * 0x100000;
            
            boolean bigger = false;
            for(int i=14; i>1; i--)
            {
                if(count[i]==3) handStrength += i * 0x011100;
                else if(count[i]==1 && !bigger) 
                {
                    bigger = true;
                    handStrength += i * 0x000010;
                    
                } else if(count[i]==1) handStrength += i;
                
            }
            
            return handStrength;
            
        } else if(pair)
        {
            handStrength += 2 * 0x100000;
            
            int cCount = 0;
            for(int i=14; i>1; i--)
            {
                if(count[i]==2) handStrength += i * 0x011000;
                else if(count[i]==1 && cCount==0) {cCount++; handStrength += i * 0x000100;}
                else if(count[i]==1 && cCount==1) {cCount++; handStrength += i * 0x000010;}
                else if(count[i]==1) handStrength += i;
            }
            
            return handStrength;
        }
        
        flush = true;
        Suit fSuit = hand[0].suit;
        for(int i=1; i<5; i++) 
        {
            if(hand[i].suit!=fSuit)
            {
                flush  = false;
                break;
            }
        }
        
        if(count[14]==1 && count[2]==1 && count[3]==1 && count[4]==1 && count[5]==1)
        {
            if(flush) return 0x954321;
            
            return 0x554321;
        }
        
        
        for(int i=14; i>1; i--)
        {
            if(count[i]==1)
            {
                for(int j=i-1; j>i-5; j--)
                {
                    if(count[j]==0) break;
                    if(j==i-4) straight = true;
                }
                break;
            }
        }
        
        if(straight && flush)
        {
            handStrength = 9 * 0x100000;
            
            for(int i=14; i>1; i--)
            {
                if(count[i]==1)
                {
                    handStrength += i * 0x010000 + (i-1) * 0x001000 + (i-2) * 0x000100;
                    handStrength += (i-3) * 0x000010 + (i-4);
                    break;
                }
            }
            
            
        } else if(flush)
        {
            handStrength += 6 * 0x100000;
            
            int cCount = 0;
            
            for(int i=14; i>1; i--)
            {
                if(count[i]==1)
                {
                    if(cCount==0) handStrength += i * 0x010000;
                    else if(cCount==1) handStrength += i * 0x001000;
                    else if(cCount==2) handStrength += i * 0x000100;
                    else if(cCount==3) handStrength += i * 0x000010;
                    else {handStrength += i; break;}
                    cCount++;
                }
            }
            
        } else if(straight)
        {
            handStrength += 5 * 0x100000;
            
            for(int i=14; i>1; i--)
            {
                if(count[i]==1)
                {
                    handStrength += i * 0x010000 + (i-1) * 0x001000 + (i-2) * 0x000100;
                    handStrength += (i-3) * 0x000010 + (i-4);
                    break;
                }
            }
            
            
        } else
        {
            handStrength += 0x100000;
            
            int cCount = 0;
            
            for(int i=14; i>1; i--)
            {
                if(count[i]==1)
                {
                    if(cCount==0) handStrength += i * 0x010000;
                    else if(cCount==1) handStrength += i * 0x001000;
                    else if(cCount==2) handStrength += i * 0x000100;
                    else if(cCount==3) handStrength += i * 0x000010;
                    else {handStrength += i; break;}
                    cCount++;
                }
            }
        }
        
        
        
        return handStrength;
    }
}

