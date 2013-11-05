package grape.dataio;


public interface DelayableOutput extends Output {
	Output createDelayedOutput(int size);
}
