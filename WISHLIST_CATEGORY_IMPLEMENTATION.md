# Wishlist Category Tabs - Dynamic Implementation

## Overview

The category tabs in the Wishlist screen have been updated to dynamically load from the API instead of being hardcoded.

## API Endpoint

- **URL**: `https://kibo-cbpk.onrender.com/api/Categories`
- **Method**: GET
- **Response Format**:

```json
{
  "data": [
    {
      "categoryid": 2,
      "categoryname": "Fuhlen",
      "productCount": 0
    },
    {
      "categoryid": 3,
      "categoryname": "Akko",
      "productCount": 0
    },
    ...
  ]
}
```

## Changes Made

### 1. Layout File (`activity_wishlist.xml`)

**Before**: Fixed tabs (Tất cả, AKKO, AULA, ASUS) hardcoded in XML
**After**: Dynamic container using `HorizontalScrollView` with empty `LinearLayout`

```xml
<HorizontalScrollView
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:scrollbars="none">

    <LinearLayout
        android:id="@+id/tabs_container"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:orientation="horizontal">
        <!-- Tabs will be added dynamically -->
    </LinearLayout>

</HorizontalScrollView>
```

**Benefits**:

- Supports unlimited number of categories
- Horizontal scroll for many categories
- Automatically updates when categories change in API

### 2. Activity File (`WishlistActivity.java`)

#### New Imports

```java
import android.widget.LinearLayout;
import com.example.kibo.models.Category;
import com.example.kibo.models.CategoryResponse;
```

#### New Fields

```java
private LinearLayout tabsContainer;
private List<TextView> categoryTabs = new ArrayList<>();
```

#### New Methods

**`loadCategories()`**

- Calls API endpoint `getCategories()`
- Loads category data from server
- Passes data to `setupCategoryTabs()`
- Handles errors gracefully (shows "Tất cả" tab only if API fails)

**`setupCategoryTabs(List<Category> categories)`**

- Creates "Tất cả" (All) tab first
- Dynamically creates tabs for each category from API
- Uses `categoryname` field from API response
- Stores all tabs in `categoryTabs` list for easy management

**`createTabView(String displayText, String categoryName, boolean isSelected)`**

- Creates individual tab TextView programmatically
- Sets text size, padding, margins (same as original design)
- Applies selected/unselected styles
- Attaches click listener for category filtering

**`resetTabStyles()`**

- Updated to loop through dynamic `categoryTabs` list
- Resets all tabs to unselected state

#### Updated Flow

1. `onCreate()` → `loadCategories()` → fetches from API
2. API response → `setupCategoryTabs()` → creates tab views
3. User clicks tab → `selectTab()` → filters products
4. Filter → `filterByCategory()` → shows filtered results

## Features

✅ Dynamic category loading from API
✅ Automatic "Tất cả" (All) tab
✅ Horizontal scrolling for many categories
✅ Same visual design as before
✅ Error handling (fallback to "All" tab only)
✅ Maintains existing filter functionality
✅ Click to filter products by category

## Testing Checklist

- [ ] Categories load from API on app start
- [ ] "Tất cả" tab appears first and is selected by default
- [ ] All categories from API appear as tabs
- [ ] Horizontal scroll works if many categories
- [ ] Clicking a tab filters products correctly
- [ ] Selected tab shows red underline (`bg_tab_selected`)
- [ ] Unselected tabs show gray color
- [ ] Search works with category filter
- [ ] App handles API errors gracefully

## Future Enhancements

- Add loading indicator while fetching categories
- Cache categories to reduce API calls
- Pull-to-refresh to reload categories
- Show category product count on tabs
