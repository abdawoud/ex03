package saarland.cispa.trust.cispabay.models;

public class Item {

    private int mId;
    private String mTitle;
    private String mDescription;
    private String mImagePath;
    private int mPrice;
    private double mLatitude;
    private double mLongitude;

    public Item(int mId, String title, String description, String imagePath, int price, double latitude, double longitude) {
        this.setId(mId);
        this.setTitle(title);
        this.setDescription(description);
        this.setImagePath(imagePath);
        this.setPrice(price);
        this.setLatitude(latitude);
        this.setLongitude(longitude);
    }

    public int getId() {
        return mId;
    }

    public void setId(int mId) {
        this.mId = mId;
    }

    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String mTitle) {
        this.mTitle = mTitle;
    }

    public String getDescription() {
        return mDescription;
    }

    public void setDescription(String mDescription) {
        this.mDescription = mDescription;
    }

    public String getImagePath() {
        return mImagePath;
    }

    public void setImagePath(String mImagePath) {
        this.mImagePath = mImagePath;
    }

    public int getPrice() {
        return mPrice;
    }

    public void setPrice(int mPrice) {
        this.mPrice = mPrice;
    }

    public double getLatitude() {
        return mLatitude;
    }

    public void setLatitude(double mLatitude) {
        this.mLatitude = mLatitude;
    }

    public double getLongitude() {
        return mLongitude;
    }

    public void setLongitude(double mLongitude) {
        this.mLongitude = mLongitude;
    }
}
