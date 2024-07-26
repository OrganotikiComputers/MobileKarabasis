package organotiki.mobile.mobilestreet;

import android.content.Context;
import android.graphics.Typeface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.TextView;

import java.util.HashMap;
import java.util.List;

import organotiki.mobile.mobilestreet.objects.Bookmark;

/**
 * Created by Thanasis on 27/5/2016.
 */
public class ExpandableListAdapter extends BaseExpandableListAdapter {

    private Context _context;
    private List<String> _listDataHeader; // header titles
    // child data in format of header title, child title
    private HashMap<String, List<String>> _listDataChild;
    private HashMap<String, List<String>> _listDataSecondChild;

    public ExpandableListAdapter(Context context, List<String> listDataHeader,
                                 HashMap<String, List<String>> listChildData,
                                 HashMap<String, List<String>> listDataSecondChild) {
        try {
            this._context = context;
            this._listDataHeader = listDataHeader;
            this._listDataChild = listChildData;
            this._listDataSecondChild = listDataSecondChild;
        } catch (Exception e) {
            Log.e("asdfg", e.getMessage(), e);
        }
    }

    @Override
    public Object getChild(int groupPosition, int childPosititon) {
        try {
            return this._listDataChild.get(this._listDataHeader.get(groupPosition))
                    .get(childPosititon);
//            return childPosititon;
        } catch (Exception e) {
            Log.e("asdfg", e.getMessage(), e);
            return null;
        }
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public View getChildView(int groupPosition, final int childPosition,
                             boolean isLastChild, View convertView, ViewGroup parent) {

        try {
            final String childText = (String) getChild(groupPosition, childPosition);

            /* if (convertView == null) {
                LayoutInflater infalInflater = (LayoutInflater) this._context
                        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = infalInflater.inflate(R.layout.list_item, null);
            }

            TextView txtListChild = (TextView) convertView
                    .findViewById(R.id.lblListItem);

            txtListChild.setText(childText);*/

            SecondLevelExpandableListView secondLevelELV = new SecondLevelExpandableListView(_context);
            SecondLevelAdapter adapter = new SecondLevelAdapter(_context);
            adapter.setText(childText);
            adapter.setParentText((String) getGroup(groupPosition));
            secondLevelELV.setAdapter(adapter);
            secondLevelELV.setGroupIndicator(null);
//            Log.d("asdfg","1st to 2nd hi");
            return secondLevelELV;
        } catch (Exception e) {
            Log.e("asdfg", e.getMessage(), e);
            return null;
        }
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        try {
            return this._listDataChild.get(this._listDataHeader.get(groupPosition))
                    .size();
        } catch (Exception e) {
            Log.e("asdfg", e.getMessage(), e);
            return 0;
        }
    }

    @Override
    public Object getGroup(int groupPosition) {
        try {
            return this._listDataHeader.get(groupPosition);
        } catch (Exception e) {
            Log.e("asdfg", e.getMessage(), e);
            return null;
        }
    }

    @Override
    public int getGroupCount() {
        try {
            return this._listDataHeader.size();
        } catch (Exception e) {
            Log.e("asdfg", e.getMessage(), e);
            return 0;
        }
    }

    @Override
    public long getGroupId(int groupPosition) {
        try {
            return groupPosition;
        } catch (Exception e) {
            Log.e("asdfg", e.getMessage(), e);
            return 0;
        }
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded,
                             View convertView, ViewGroup parent) {
        try {
            String headerTitle = (String) getGroup(groupPosition);
            if (convertView == null) {
                LayoutInflater infalInflater = (LayoutInflater) this._context
                        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = infalInflater.inflate(R.layout.list_group, null);
            }

            TextView lblListHeader = (TextView) convertView
                    .findViewById(R.id.lblListHeader);
            lblListHeader.setTypeface(null, Typeface.BOLD);
            lblListHeader.setText(headerTitle);
//            Log.d("asdfg","1st hi");
            return convertView;
        } catch (Exception e) {
            Log.e("asdfg", e.getMessage(), e);
            return null;
        }
    }

    @Override
    public boolean hasStableIds() {
        try {
            return false;
        } catch (Exception e) {
            Log.e("asdfg", e.getMessage(), e);
            return false;
        }
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        try {
            return true;
        } catch (Exception e) {
            Log.e("asdfg", e.getMessage(), e);
            return false;
        }
    }

    public class SecondLevelExpandableListView extends ExpandableListView {

        public SecondLevelExpandableListView(Context context) {
            super(context);
        }

        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            try {
//                Log.d("asdfg","start of 2nd hi");
                //999999 is a size in pixels. ExpandableListView requires a maximum height in order to do measurement calculations.
                heightMeasureSpec = MeasureSpec.makeMeasureSpec(999999, MeasureSpec.AT_MOST);
            } catch (Exception e) {
                Log.e("asdfg", e.getMessage(), e);
            }
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        }
    }

    public class SecondLevelAdapter extends BaseExpandableListAdapter {

        private Context context;
        private String text;
        private String parentText;

        public SecondLevelAdapter(Context context) {
            this.context = context;
        }

        @Override
        public Object getGroup(int groupPosition) {
            return groupPosition;
        }

        @Override
        public int getGroupCount() {
            return 1;
        }

        @Override
        public long getGroupId(int groupPosition) {
            return groupPosition;
        }

        @Override
        public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
            try {
//                Log.d("asdfg","2nd hi");
                if (convertView == null) {
                    LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    convertView = inflater.inflate(R.layout.list_item, null);
                    TextView textView = (TextView) convertView.findViewById(R.id.lblListItem);
                    textView.setText(text);
                    convertView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            ((Bookmarks) _context).childClicked(parentText, text);
                        }
                    });
//                    convertView.setOnTouchListener(new View.OnTouchListener() {
//                        @Override
//                        public boolean onTouch(View view, MotionEvent event) {
//                            int action = event.getAction();
//                            if (action == MotionEvent.ACTION_DOWN) {
//                                ((Bookmarks) _context).childClicked(parentText, text);
//                            }
//                            return false;
//                        }
//                    });
                }
                //Log.d("asdfg", "GroupPosition="+groupPosition+", isExpanded="+isExpanded+", convertView="+convertView+", parent="+parent);
                return convertView;
            } catch (Exception e) {
                Log.e("asdfg", e.getMessage(), e);
                return null;
            }
        }

        @Override
        public Object getChild(int groupPosition, int childPosititon) {
            try {
                return _listDataSecondChild.get(text)
                        .get(childPosititon);
//            return childPosititon;
            } catch (Exception e) {
                Log.e("asdfg", e.getMessage(), e);
                return null;
            }
        }

//        @Override
//        public Object getChild(int groupPosition, int childPosition) {
//            return childPosition;
//        }

        @Override
        public long getChildId(int groupPosition, int childPosition) {
            return childPosition;
        }

        @Override
        public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
            try {
//                Log.d("asdfg","3rd hi");
                if (convertView == null) {
                    LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    convertView = inflater.inflate(R.layout.list_second_item, null);
                    TextView textView = (TextView) convertView.findViewById(R.id.lblListItem);
                    final String childText = String.valueOf(getChild(groupPosition, childPosition));

                    convertView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            ((Bookmarks) _context).childClicked(text, childText);
                        }
                    });
//                    convertView.setOnTouchListener(new View.OnTouchListener() {
//                        public boolean onTouch(View view, MotionEvent event) {
//                            int action = event.getAction();
//                            if (action == MotionEvent.ACTION_DOWN) {
//                                ((Bookmarks) _context).childClicked(text, childText);
//                            }
//                            return false;
//                        }
//                    });

                    textView.setText(childText);
                    //                if (convertView == null) {
                    //                    LayoutInflater infalInflater = (LayoutInflater) _context
                    //                            .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    //                    convertView = infalInflater.inflate(R.layout.list_item, null);
                    //                }
                    //
                    //                TextView txtListChild = (TextView) convertView
                    //                        .findViewById(R.id.lblListItem);
                    //
                    //                txtListChild.setText(childText);
                }
                //Log.d("asdfg", "GroupPosition="+groupPosition+", childPosition="+childPosition+", isLastChild="+isLastChild+", convertView="+convertView+", parent="+parent);
                return convertView;
            } catch (Exception e) {
                Log.e("asdfg", e.getMessage(), e);
                return null;
            }
        }

        @Override
        public int getChildrenCount(int groupPosition) {
            try {
                return _listDataSecondChild.get(text)
                        .size();
            } catch (Exception e) {
                Log.e("asdfg", e.getMessage(), e);
                return 0;
            }
        }


        @Override
        public boolean hasStableIds() {
            return true;
        }

        @Override
        public boolean isChildSelectable(int groupPosition, int childPosition) {
            return true;
        }

        public void setText(String text) {
            this.text = text;
        }

        public String getParentText() {
            return parentText;
        }

        public void setParentText(String parentText) {
            this.parentText = parentText;
        }
    }
}