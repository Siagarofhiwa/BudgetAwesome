# 💰 Budget Awesome - POE Final Submission

A fully-featured Android Budget Tracking App built with Kotlin and RoomDB.
Runs on Android API 24+ (Android 7.0 and above).

## 🎥 Demo Video
[Watch on YouTube](YOUR_YOUTUBE_LINK_HERE)

---

## 📱 All Features

### Core Features (Part 2)
| Feature | Description |
|---|---|
| Login & Register | Secure authentication stored in RoomDB |
| Categories | Create colour-coded expense categories |
| Add Expense | Log expenses with date, start/end time, description, amount, category |
| Photo | Attach a photo from camera or gallery to any expense |
| Expense List | Filter expenses by user-selectable date period |
| Goals | Set minimum and maximum monthly spending goals |
| Dashboard | Monthly overview with category breakdown and progress bars |

### New Features (Part 3 / POE)
| Feature | Description |
|---|---|
| 📊 Graph | Bar chart of spending per category with min/max goal lines |
| 📈 Visual Goal Progress | Colour-coded progress bar showing budget status (green/orange/red) |
| 🏆 Gamification / Badges | 8 earnable badges for meeting goals and logging expenses |
| 💡 Smart Budget Tips (Own Feature 1) | Personalised tips based on real spending data |
| 🔍 Search Expenses (Own Feature 2) | Real-time search across all expenses by description or category |

---

## 🏅 Own Feature 1 — Smart Budget Tips

**Location:** Tips tab in the bottom navigation bar

**Description:**
The Smart Budget Tips feature analyses the user's spending data from RoomDB and generates personalised financial tips in real time. Tips are based on:
- Whether the user is over or under budget
- Their highest spending category
- How many expenses they have logged
- Whether they have set a goal
- General financial best practices (random tip each visit)

This feature helps users understand their financial behaviour and make better decisions without needing to manually interpret data.

---

## 🔍 Own Feature 2 — Search Expenses

**Location:** Search tab in the bottom navigation bar

**Description:**
The Search Expenses feature allows users to search through all their expense entries in real time as they type. The search matches against:
- Expense description
- Category name

Results update instantly with each keystroke. The total amount of all matching results is shown at the top. This makes it easy to find specific expenses without scrolling through a long list.

---

## 🏆 Gamification Badges

The app awards 8 badges to encourage consistent and responsible budgeting:

| Badge | Icon | How to Earn |
|---|---|---|
| First Steps | 🏅 | Log your first expense |
| Expense Explorer | 🌟 | Log 5 expenses |
| Power Tracker | 💎 | Log 10 expenses |
| Goal Setter | 🎯 | Set a budget goal |
| Budget Hero | 💚 | Stay within max budget for the month |
| Savings Champion | 🏆 | Spend under the minimum goal |
| Photogenic | 📸 | Attach a photo to an expense |
| Organised | 🗂️ | Create 3 or more categories |

---

## 🏗️ Tech Stack

| Technology | Purpose |
|---|---|
| Kotlin | Primary programming language |
| RoomDB (SQLite) | Local offline database |
| LiveData | Reactive UI updates |
| ViewBinding | Type-safe view access |
| Coroutines | Async database operations |
| MPAndroidChart | Bar chart for spending graph |
| FileProvider | Secure camera photo sharing |
| SharedPreferences | User session management |

---

## 📂 Project Structure

```
app/src/main/java/com/budgetawesome/
├── data/
│   ├── entity/     User, Category, Expense, Goal, Badge
│   ├── dao/        UserDao, CategoryDao, ExpenseDao, GoalDao, BadgeDao
│   └── AppDatabase.kt
├── ui/
│   ├── LoginActivity.kt
│   ├── MainActivity.kt
│   ├── DashboardFragment.kt
│   ├── CategoryFragment.kt
│   ├── AddExpenseActivity.kt
│   ├── ExpenseListFragment.kt
│   ├── GoalsFragment.kt
│   ├── GraphFragment.kt       ← NEW: Bar chart with goal lines
│   ├── BadgesFragment.kt      ← NEW: Gamification badges
│   ├── TipsFragment.kt        ← NEW: Own Feature 1
│   ├── SearchFragment.kt      ← NEW: Own Feature 2
│   └── PhotoViewActivity.kt
├── adapter/
│   ├── CategoryAdapter.kt
│   ├── ExpenseAdapter.kt
│   └── BadgeAdapter.kt        ← NEW
└── util/
    ├── SessionManager.kt
    └── BadgeEngine.kt         ← NEW: Badge award logic
```

---

## 🚀 How to Run

1. Clone: `git clone https://github.com/Siagarofhiwa/BudgetAwesome.git`
2. Open in Android Studio
3. Sync Gradle
4. Run on device (API 24+) or emulator
5. Login: **admin / admin123**

---

## 📸 Screenshots

> _(Add screenshots here after recording demo video)_

---

## 📚 References

- Android Developer Docs: https://developer.android.com
- Room Database: https://developer.android.com/training/data-storage/room
- MPAndroidChart: https://github.com/PhilJay/MPAndroidChart
- Camera API: https://developer.android.com/training/camera/photobasics
- RecyclerView: https://developer.android.com/guide/topics/ui/layout/recyclerview
- LiveData: https://developer.android.com/topic/libraries/architecture/livedata
- Gamification: https://developer.android.com/games

---

## 👨‍💻 Author

**Rofhiwa Siaga** | ST10382569
The Independent Institute of Education (IIE) 2026
