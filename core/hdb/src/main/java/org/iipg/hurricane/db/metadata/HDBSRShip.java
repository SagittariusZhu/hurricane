package org.iipg.hurricane.db.metadata;

public class HDBSRShip extends HDBBaseObject {
		private String type;

		private String snode;
		private String enode;
		public String getSnode() {
			return snode;
		}
		public void setSnode(String snode) {
			this.snode = snode;
		}
		public String getEnode() {
			return enode;
		}
		public void setEnode(String enode) {
			this.enode = enode;
		}
		public String getType() {
			return type;
		}
		public void setType(String type) {
			this.type = type;
		}
}
