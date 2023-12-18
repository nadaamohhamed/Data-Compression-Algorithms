package Handlers;

public abstract class CompressionTechniqueHandler {
    protected FileHandler fileHandler;
    protected ImageHandler imageHandler;
    public CompressionTechniqueHandler(FileHandler fileHandler) {
        this.fileHandler = fileHandler;
    }
    public CompressionTechniqueHandler(ImageHandler imageHandler) {
        this.imageHandler = imageHandler;
    }

    public CompressionTechniqueHandler(FileHandler fileHandler, ImageHandler imageHandler) {
        this.fileHandler = fileHandler;
        this.imageHandler = imageHandler;
    }

    public abstract void compress(String file);
    public abstract void decompress(String file);

}
