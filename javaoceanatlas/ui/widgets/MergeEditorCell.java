package javaoceanatlas.ui.widgets;


public class MergeEditorCell {
  private String mValue;
  private boolean mCellEdited = false;
  private String mOrigValue;

  public MergeEditorCell(String value) {
    mValue = value;
    mOrigValue = new String(mValue);
  }

  public String toString() {
    return mValue;
  }

  public String getValue() {
    return mValue;
  }

  public void setValue(String s) {
    mValue = s;
  }

  public void setNewValue(String s) {
    mValue = s;
    setEdited(true);
  }

  public void setEdited(boolean b) {
    mCellEdited = b;
  }

  public boolean isEdited() {
    return mCellEdited;
  }

  public void updateCellValues() {
    this.setValue(mValue);
  }

  public void revertCellValues() {
    mValue = mOrigValue;
    mCellEdited = false;
  }
}
