/*
FRODO: a FRamework for Open/Distributed Optimization
Copyright (C) 2008-2020  Thomas Leaute, Brammert Ottens & Radoslaw Szymanek

FRODO is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

FRODO is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.


How to contact the authors: 
<https://frodo-ai.tech>
*/

package frodo2.benchmarks.auctions.cats;

import java.util.ArrayList;
import java.util.List;

/**
 * This class represents an auction generated by the CATS program
 * 
 * @author Andreas Schaedeli
 *
 */
public class Auction {

	/**List containing all the goods sold in this auction*/
	private List<Good> goodsList;
	
	/**List containing all the bids placed in this auction*/
	private List<Bid> bidsList;
	
	/**List containing all the bidders of this auction*/
	private List<Bidder> biddersList;
	
	
	/**
	 * The constructor first initializes all the lists used in this class. Then, it calls a method to create as many goods as desired and places them in
	 * the corresponding list. 
	 * 
	 * @param nbGoods Number of goods sold in this auction
	 */
	public Auction(int nbGoods) {
				
		goodsList = new ArrayList<Good>(nbGoods);
		bidsList = new ArrayList<Bid>();
		biddersList = new ArrayList<Bidder>();
		Bidder.NEXT_ID = 0;
				
		createGoodsList(nbGoods);
	}
	
	/**
	 * This method adds a bid to the auction. A bid needs to be added in several places: First of all, to the bids list. Then, to the bids list of the
	 * corresponding bidder. And finally, to the bids lists of the goods contained in the bid
	 * 
	 * @param bid Bid to add to the auction
	 */
	public void addBid(Bid bid) {
		bidsList.add(bid);
		bid.getBidder().addBid(bid);
		for(Good good : bid.getGoodsList()) {
			good.addBid(bid);
		}
	}
	
	/**
	 * This method adds a new Bidder to the list of bidders in this auction
	 * 
	 * @param bidder Bidder to add to the auction
	 */
	public void addBidder(Bidder bidder) {
		biddersList.add(bidder);
	}
	
	/** 
	 * @param goodID ID of the good to be returned
	 * @return Good object with the given ID
	 */
	public Good getGood(int goodID) {
		return goodsList.get(goodID);
	}
	
	/**
	 * @return List of goods offered in this auction
	 */
	public List<Good> getGoods() {
		return goodsList;
	}
	
	/**
	 * @return List of bids placed in this auction
	 */
	public List<Bid> getBids() {
		return bidsList;
	}
	
	/**
	 * @return List of all bidders in this auction
	 */
	public List<Bidder> getBidders() {
		return biddersList;
	}
	
	/**
	 * This method creates The desired number of goods having IDs from 0 to (nbGoods - 1) and adds them to the goodsList
	 * 
	 * @param nbGoods Number of goods to create
	 */
	private void createGoodsList(int nbGoods) {
		for(int i = 0; i < nbGoods; i++) {
			goodsList.add(new Good(i));
		}
	}
	
	/** 
	 * @see java.lang.Object#toString() 
	 * @author Thomas Leaute
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder ("Auction\n");
		
		builder.append("\tGoods:\n");
		for (Good good : this.goodsList) 
			builder.append("\t\t").append(good).append("\n");
		
		builder.append("\tBidders:\n");
		for (Bidder bidder : this.biddersList) {
			builder.append("\t\t").append(bidder).append("\n");
			
			builder.append("\t\t\tBids:\n");
			for (Bid bid : bidder.getBidsList()) 
				builder.append("\t\t\t\t").append(bid).append("\n");
		}
		
		return builder.toString();
	}
}
