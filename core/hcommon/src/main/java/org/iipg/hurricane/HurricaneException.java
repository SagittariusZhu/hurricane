/**
 * 
 */
package org.iipg.hurricane;

/**
 * @author lixiaojing
 *
 */
public class HurricaneException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public HurricaneException(String msg, Throwable root) {
		super( msg, root );
	}

	public HurricaneException(Throwable root) {
		super(root);
	}

	public HurricaneException(String s) {
		super(s);
	}
}
