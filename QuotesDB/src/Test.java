
import com.manticore.database.Quotes;
import com.manticore.foundation.Position;
import java.util.Iterator;
import java.util.ArrayList;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author are
 */
public class Test {
    public static void main(String[] args) {
        ArrayList<Position> positionArrayList=Quotes.getInstance().getPositionArrayList(false);
        Iterator<Position> positionIterator=positionArrayList.iterator();
        //calculate max drawdown

        float drawdown_amount=0f;
        float max_drawdown_amount=0f;
        int drawdown_count=0;
        int max_drawdown_count=0;



        int profit_trades=0;
        float profit=0;

        int loss_trades=0;
        float loss=0f;

        while (positionIterator.hasNext()) {
            Position position=positionIterator.next();


            if (position.profit<0) {
                loss_trades++;
                loss+=position.profit;

                drawdown_amount+=position.profit;
                drawdown_count++;

            } else {
                profit_trades++;
                profit+=position.profit;

                if (max_drawdown_amount>drawdown_amount) max_drawdown_amount=drawdown_amount;
                if (max_drawdown_count<drawdown_count) max_drawdown_count=drawdown_count;

                drawdown_amount=0f;
                drawdown_count=0;
            }
            //System.out.println("drawdown: " + position.profit + "; " + drawdown_amount + " " + drawdown_count);
        }

        if (max_drawdown_amount>drawdown_amount) max_drawdown_amount=drawdown_amount;
        if (max_drawdown_count<drawdown_count) max_drawdown_count=drawdown_count;

        System.out.println("drawdown: " + max_drawdown_amount + "; " + max_drawdown_count + " trades");
        System.out.println("loss: " + loss + " (" + loss_trades + "); " + profit + " (" + profit_trades + ")");
        
    }


}
