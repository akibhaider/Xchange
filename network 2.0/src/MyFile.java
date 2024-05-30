
public class MyFile {
    private int id;
    private String name;
    private byte[] data;
    private String file_extension;

    public MyFile(int id, String name, byte[] data, String file_extension) {
        this.id = id;
        this.name = name;
        this.data = data;
        this.file_extension = file_extension;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public void setFile_extension(String file_extension) {
        this.file_extension = file_extension;
    }

    public int getId() {
        return this.id;
    }

    public String getName() {
        return this.name;
    }

    public byte[] getData() {
        return this.data;
    }

    public String getFile_extension() {
        return this.file_extension;
    }
}


