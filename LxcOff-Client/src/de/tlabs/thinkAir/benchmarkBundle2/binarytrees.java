/* The Great Computer Language Shootout
http://shootout.alioth.debian.org/

contributed by Jarkko Miettinen
 */

/*
 Each program should
 define a tree node class and methods, a tree node record and procedures, or an algebraic data type and functions, or�
 allocate a binary tree to 'stretch' memory, check it exists, and deallocate it
 allocate a long-lived binary tree which will live-on while other trees are allocated and deallocated
 allocate, walk, and deallocate many bottom-up binary trees
 allocate a tree
 walk the tree nodes, checksum node items (and maybe deallocate the node)
 deallocate the tree
 check that the long-lived binary tree still exists
 */
package de.tlabs.thinkAir.benchmarkBundle2;

import java.lang.reflect.Method;

import de.tlabs.thinkAir.benchmarkBundle1.Benchmark;
import org.jason.lxcoff.lib.ExecutionController;
import org.jason.lxcoff.lib.Remote;
import org.jason.lxcoff.lib.Remoteable;

public class binarytrees extends Remoteable implements Benchmark {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6078977270772098088L;
	private final static int minDepth = 4;
	private transient ExecutionController controller;
	
	public binarytrees(ExecutionController controller){
		this.controller = controller;
	}

	public void main(int n) {
		int maxDepth = (minDepth + 2 > n) ? minDepth + 2 : n;
		int stretchDepth = maxDepth + 1;

		System.out.print(treeCheck(stretchDepth, maxDepth));
	}

	@Remote
	private String localtreeCheck(int stretchDepth, int maxDepth) {
		StringBuffer output = new StringBuffer();
		int check = (TreeNode.bottomUpTree(0, stretchDepth)).itemCheck();
		output.append("stretch tree of depth " + stretchDepth + "\t check: "
				+ check + "\n");

		TreeNode longLivedTree = TreeNode.bottomUpTree(0, maxDepth);

		for (int depth = minDepth; depth <= maxDepth; depth += 2) {
			int iterations = 1 << (maxDepth - depth + minDepth);
			check = 0;

			for (int i = 1; i <= iterations; i++) {
				check += (TreeNode.bottomUpTree(i, depth)).itemCheck();
				check += (TreeNode.bottomUpTree(-i, depth)).itemCheck();
			}
			output.append((iterations * 2) + "\t trees of depth " + depth
					+ "\t check: " + check + "\n");
		}
		output.append("long lived tree of depth " + maxDepth + "\t check: "
				+ longLivedTree.itemCheck() + "\n");
		return output.toString();
	}

	private static class TreeNode {
		private TreeNode left, right;
		private int item;

		TreeNode(int item) {
			this.item = item;
		}

		private static TreeNode bottomUpTree(int item, int depth) {
			if (depth > 0) {
				return new TreeNode(bottomUpTree(2 * item - 1, depth - 1),
						bottomUpTree(2 * item, depth - 1), item);
			} else {
				return new TreeNode(item);
			}
		}

		TreeNode(TreeNode left, TreeNode right, int item) {
			this.left = left;
			this.right = right;
			this.item = item;
		}

		private int itemCheck() {
			// if necessary deallocate here
			if (left == null)
				return item;
			else
				return item + left.itemCheck() - right.itemCheck();
		}
	}

	@Override
	public void copyState(Remoteable arg0) {
		// TODO Auto-generated method stub

	}

	private  String treeCheck (int stretchDepth, int maxDepth) {
	       Method toExecute;
	       Class<?>[] paramTypes = {int.class,int.class};
	       Object[] paramValues = { stretchDepth, maxDepth};
	       String result = null;
	       Object genResult = null;
	       try {
	           toExecute = this.getClass().getDeclaredMethod("localtreeCheck", paramTypes);
	           genResult = controller.execute(toExecute, paramValues, this);
	           result = (String) genResult;
	       } catch (SecurityException e) {
	           // Should never get here
	           e.printStackTrace();
	           throw e;
	       } catch (NoSuchMethodException e) {
	           // Should never get here
	           e.printStackTrace();
	       } catch (Throwable e) {
	           // TODO Auto-generated catch block
	           e.printStackTrace();
	           System.out.print(genResult.toString());
	       }
	       return result;
	   }
}
