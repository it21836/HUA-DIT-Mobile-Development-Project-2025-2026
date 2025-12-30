## DailyTasks (Student-level setup)

### Απαιτήσεις
- Android Studio Giraffe/μεταγενέστερο
- JDK 17
- Android SDK Platform 34
- Ενεργό Android emulator ή φυσική συσκευή με USB debugging

### Πώς τρέχεις το project
1. Άνοιξε το φάκελο στο Android Studio (`File > Open` στο root).
2. Άσε το Gradle sync να ολοκληρωθεί (κατεβάζει dependencies Room/WorkManager κ.λπ.).
3. Επίλεξε συσκευή/emulator και πάτα Run.

### Αν δεν υπάρχει gradle wrapper
- Τρέξε από τερματικό στο root:  
  `gradle wrapper`  
  και ξανα-άνοιξε το project στο Studio.

### Συνήθη Προβλήματα

#### Σφάλμα Gradle "Unable to find method"
Αν αντιμετωπίσετε ένα σφάλμα Gradle με το μήνυμα `Unable to find method '''org.gradle.api.file.FileCollection org.gradle.api.artifacts.Configuration.fileCollection(org.gradle.api.specs.Spec)'''`, αυτό πιθανότατα οφείλεται σε ασυμβατότητα μεταξύ της έκδοσης του Gradle και του Android Gradle Plugin.

**Λύση:**
1.  Ανοίξτε το αρχείο `gradle/wrapper/gradle-wrapper.properties`.
2.  Βεβαιωθείτε ότι η ιδιότητα `distributionUrl` χρησιμοποιεί μια σταθερή και συμβατή έκδοση του Gradle. Για παράδειγμα:
    `distributionUrl=https\://services.gradle.org/distributions/gradle-8.13-bin.zip`
3.  Κάντε "Sync Project with Gradle Files" στο Android Studio.
