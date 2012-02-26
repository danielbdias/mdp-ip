package graph.gviz;

public class DotViewerDemo extends DotViewer
{

    // Must override
    public void nodeClicked(String name) {
	displayText("Clicked node: " + name);
    }

    // Make this runnable
    public static void main(String args[]) {

	if(args.length > 1) {
	    System.err.println("USAGE: java graph.gviz.DotViewerDemo [input_graph_file]");
	    System.exit(1);
	} 

	DotViewerDemo demo = new DotViewerDemo();
	demo.setWindowSizing(800, 600, 100, 100, 20);
	demo.showWindow(args[0]);

	//DotViewerDemo demo2 = new DotViewerDemo();
	//demo2.setWindowSizing(800, 600, 100, 100, 20);
	//demo2.showWindow(args[0]);
    }

}
