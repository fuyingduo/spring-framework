package com.f7o.arithmetic;

/**
 * 冒泡排序算法
 * 原理
 * <p>
 * 第二层循环相邻比较
 * 在每次第一层循环完后将最大值放入最右侧，数组最大下表位置
 * 在第二次二层循环就不需要取将最大下表值比较，以此类推就实现了冒泡排序
 * <p>
 * 如果实现倒序冒泡排序 则只需要将判断targets[j] < targets[j + 1] 即可
 * <p>
 * created by fuyd on 2020-06-17
 */
public class BubbleSort {


	public static int[] execute(int[] targets) {

		int tempSpace;

		// 第一层循环
		for (int i = 0; i < targets.length - 1; i++) {

			// 第二层循环
			for (int j = 0; j < targets.length - 1 - i; j++) {

				if (targets[j] > targets[j + 1]) {

					tempSpace = targets[j];
					targets[j] = targets[j + 1];
					targets[j + 1] = tempSpace;
				}
			}
		}
		return targets;
	}

}
