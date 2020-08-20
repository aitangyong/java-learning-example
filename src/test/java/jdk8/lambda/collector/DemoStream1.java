package jdk8.lambda.collector;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class DemoStream1 {

	private static class ViewLayerCard {
		private int cardId;
		private int sort;

		public ViewLayerCard(int cardId, int sort) {
			this.cardId = cardId;
			this.sort = sort;
		}

		public int getCardId() {
			return cardId;
		}

		public int getSort() {
			return sort;
		}

	}

	public static void main(String[] args) {
		List<ViewLayerCard> cards = new ArrayList<>();
		cards.add(new ViewLayerCard(100, 1));
		cards.add(new ViewLayerCard(200, 2));
		cards.add(new ViewLayerCard(300, 2));
		cards.add(new ViewLayerCard(400, 3));
		cards.add(new ViewLayerCard(500, 3));

		// sort -> ViewLayerCard列表
		Map<Integer, List<ViewLayerCard>> groupBySort1 = cards.stream()
				.collect(Collectors.groupingBy(ViewLayerCard::getSort, Collectors.toList()));
		System.out.println(groupBySort1);

		// sort -> cardId列表
		Map<Integer, List<Integer>> groupBySort2 = cards.stream().collect(Collectors.groupingBy(ViewLayerCard::getSort,
				Collectors.mapping(ViewLayerCard::getCardId, Collectors.toList())));
		// {1=[100], 2=[200, 300], 3=[400, 500]}
		System.out.println(groupBySort2);

	}
}
