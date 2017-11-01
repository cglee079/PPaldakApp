package com.example.changoo.fishing.util;

import com.example.changoo.fishing.model.Fish;

import java.util.Comparator;

/**
 * Created by changoo on 2017-03-12.
 */

public class Sort {

	public static Comparator<Fish> maxDesc = new Comparator<Fish>() { // Max로
		@Override
		public int compare(Fish item1, Fish item2) {
			int ret = 0;

			if (item1.getMaxFower() < item2.getMaxFower()) {
				ret = 1;
			} else if (item1.getMaxFower() == item2.getMaxFower()){
				ret = 0;
			} else {
				ret = -1;
			}
			return ret;

		}
	};

	public static Comparator<Fish> avgDesc = new Comparator<Fish>() {
		@Override
		public int compare(Fish item1, Fish item2) {
			int ret = 0;

			if (item1.getAvgFower() < item2.getAvgFower()){
				ret = 1;
			} else if (item1.getAvgFower() == item2.getAvgFower()){
				ret = 0;
			} else {
				ret = -1;
			}
			return ret;

		}
	};

	public static Comparator<Fish> nameAsc = new Comparator<Fish>() { // name으로
		@Override
		public int compare(Fish arg0, Fish arg1) {
			return arg0.getName().compareTo(arg1.getName());
		}
	};

	public static Comparator<Fish> speciesAsc = new Comparator<Fish>() { // speces으로
		@Override
		public int compare(Fish arg0, Fish arg1) {
			return arg0.getSpecies().compareTo(arg1.getSpecies());
		}
	};

	public static Comparator<Fish> datetimeAsc = new Comparator<Fish>() {
		@Override
		public int compare(Fish arg0, Fish arg1) {
			String datetime = arg0.getDate() + arg0.getTime();
			return datetime.compareTo(arg1.getDate() + arg1.getTime());
		}
	};
}
