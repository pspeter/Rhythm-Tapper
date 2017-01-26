package sma.rhythmtapper.models;

public class BallData {
    private int _titleId = -1;
    private int _descId = -1;
    private int _imgId = -1;

    public BallData(int titleId, int descId, int imgId){
        this._titleId = titleId;
        this._descId = descId;
        this._imgId = imgId;
    }
    public int getTitleId() {
        return _titleId;
    }

    public void setTitleId(int _titleId) {
        this._titleId = _titleId;
    }

    public int getDescId() {
        return _descId;
    }

    public void setDescId(int _descid) {
        this._descId = _descid;
    }

    public int getImgId() {
        return _imgId;
    }

    public void setImgId(int _imgId) {
        this._imgId = _imgId;
    }
}
