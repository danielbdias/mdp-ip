package add;

public abstract class TerminalNodeKey extends NodeKey{
  double errorMergeNode;
  public double getErrorMerge(){
	  return this.errorMergeNode;
  }
  public void setErrorMerge(double errorMerge){
	  this.errorMergeNode=errorMerge;
  }
}

