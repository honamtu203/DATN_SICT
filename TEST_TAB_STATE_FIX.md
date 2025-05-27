# Test Tab State Preservation Fix

## 🎯 Mục tiêu
Sửa lỗi tab luôn reset về "Khoản chi" khi back từ FragmentListData về FragmentReport

## 🔧 Các thay đổi đã thực hiện

### 1. ReportViewModel.kt
- ✅ Thay đổi `typeReport` từ `SingleLiveData` → `MutableLiveData`
- ✅ Thay đổi `dataRcv` từ `SingleLiveData` → `MutableLiveData`  
- ✅ Thay đổi `dataIncomeRcv` từ `SingleLiveData` → `MutableLiveData`
- ✅ Thêm logging để debug

### 2. FragmentReport.kt
- ✅ Thêm method `restoreTabState()` để khôi phục trạng thái tab
- ✅ Cải thiện logic trong `onViewCreated()` để kiểm tra dữ liệu có sẵn
- ✅ Tách riêng `setUpTabLayoutListener()` để có thể gọi lại
- ✅ Cải thiện `observeExpenseData()` và `observeIncomeData()` với observer cleanup
- ✅ Thêm extensive logging để debug
- ✅ Sử dụng `post{}` để đảm bảo TabLayout sẵn sàng

## 🔍 Logic hoạt động mới

### Khi FragmentReport được tạo (back từ FragmentListData):

1. **onViewCreated()** được gọi
   - Log current `typeReport.value`
   - Kiểm tra dữ liệu đã có sẵn chưa
   - Nếu có → gọi `restoreTabState()` ngay
   - Nếu chưa → gọi `getAllData()` rồi `restoreTabState()`

2. **restoreTabState()** được gọi
   - Log trạng thái hiện tại
   - Post to UI thread để đảm bảo TabLayout sẵn sàng
   - Clear listeners tạm thời
   - Set tab đúng với `typeReport.value`
   - Restore listeners
   - Setup observers cho tab hiện tại

3. **Observer cleanup**
   - Remove tất cả observers cũ trước khi setup mới
   - Tránh conflict giữa expense và income observers

## 🧪 Test Cases

### Test 1: Tab Expense → FragmentListData → Back
1. Mở FragmentReport (mặc định tab Expense)
2. Click vào một category expense
3. Vào FragmentListData
4. Ấn back
5. **Expected**: Vẫn ở tab Expense

### Test 2: Tab Income → FragmentListData → Back  
1. Mở FragmentReport
2. Chuyển sang tab Income
3. Click vào một category income
4. Vào FragmentListData
5. Ấn back
6. **Expected**: Vẫn ở tab Income (KHÔNG reset về Expense)

### Test 3: Multiple navigations
1. Expense → ListData → Back → Income → ListData → Back
2. **Expected**: Mỗi lần back đều giữ đúng tab

## 📋 Debug Logs để kiểm tra

Khi test, check các logs sau:

```
FragmentReport: onViewCreated - Current typeReport: [0 hoặc 1]
FragmentReport: Data already available, restoring tab state immediately
FragmentReport: Restoring tab state: [0 hoặc 1] (0=expense, 1=income)
FragmentReport: TabLayout tab count: 2
FragmentReport: Target tab: [Tab object], position: [0 hoặc 1]
FragmentReport: Tab selected successfully. Current selected: [0 hoặc 1]
FragmentReport: Initializing [expense/income] data
FragmentReport: Setting up [expense/income] observers
```

## ⚠️ Potential Issues

1. **TabLayout timing**: Nếu vẫn không work, có thể cần delay thêm
2. **Observer conflicts**: Nếu data không update, check observer cleanup
3. **ViewModel scope**: Đảm bảo dùng `activityViewModels()` không phải `viewModels()`

## 🎯 Success Criteria

- ✅ Tab UI hiển thị đúng với `typeReport.value`
- ✅ Dữ liệu hiển thị đúng với tab được chọn
- ✅ Không có reset về tab mặc định
- ✅ Hoạt động ổn định qua nhiều lần navigation 