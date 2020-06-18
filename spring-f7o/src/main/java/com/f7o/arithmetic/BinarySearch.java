package com.f7o.arithmetic;

/**
 * 二分查找发
 * created by fuyd on 2020-06-17
 */
public class BinarySearch {

	/**
	 * 使用二分查找去除数组中想要的数据
	 * <p>
	 * 实现逻辑：
	 * arr[] 数组必须正序排列
	 * arr[] 数组数据如下: {1, 2, 3, 4, 5, 6, 7, 8}
	 * key 目标数据如下 5
	 *
	 * @param arr 所有数据
	 * @param key 目标数据
	 */
	public static int execute(int[] arr, int key) {

		// 设置数组最小下标
		int low = 0;
		// 数组最大下标
		int high = arr.length - 1;
		// 目标数据下表
		int middle;

		// 1. 判断目标数据是否小于数组中最小值、或大于数组中最大值
		if (key < arr[low] || key > arr[high]) {
			return -1;
		}

		while (true) {
			// 计算数组中最小折中下表
			middle = (low + high) / 2;

			// 判断如果数组中位下表数值是否大于目标数值，如果大于目标值，则对(low)---(middle - 1)中的值继续折中查找
			if (arr[middle] > key) {
				high = middle - 1;
			}
			// 判断如果数组中卫下表数值是否小雨目标数组，如果小雨目标值，则对(middle + 1) --- (high)中的值继续折中查找
			else if (arr[middle] < key) {
				low = middle + 1;
			}
			// 如果相等则返回小表数退出while循环
			else {
				return middle;
			}
		}
	}
}
