/* =========================================
   ELVORA — WEB APP
   Local demo version
   ========================================= */

const $ = id => document.getElementById(id);

const DB_KEY = "elvora_web_v2";

const defaultDB = {
  users: [],
  currentUser: null,
  library: [],
  activities: [],
  quizzes: [],
  feedback: [],
  cardsLearnt: [],
  settings: {
    theme: "dark",
    music: false,
    volume: 35,
    reducedMotion: false,
    reminders: true
  }
};

let db = loadDB();
let authMode = "login";
let welcomeIndex = 0;
let welcomeTimer = null;
let currentQuestions = [];
let currentCards = [];
let currentCardIndex = 0;
let currentSummary = "";
let musicContext = null;
let musicOscillators = [];
let musicPlaying = false;

const welcomeSlides = [
  ["Welcome to ELVORA", "Your personal learning space."],
  ["Learn Smarter", "Make complex concepts easier to understand."],
  ["Smart MCQs", "Practice and discover your learning gaps."],
  ["FlipCards", "Remember concepts through active recall."],
  ["Smart Summary", "Organise your study material."],
  ["Your Progress", "Track your genuine learning activities."],
  ["Let's Begin", "Your learning journey starts here."]
];

/* DATABASE */

function loadDB() {
  try {
    const saved = JSON.parse(localStorage.getItem(DB_KEY));
    return saved ? { ...defaultDB, ...saved } : structuredClone(defaultDB);
  } catch {
    return structuredClone(defaultDB);
  }
}

function saveDB() {
  localStorage.setItem(DB_KEY, JSON.stringify(db));
}

function currentUser() {
  return db.users.find(u => u.email === db.currentUser) || null;
}

function userItems() {
  return db.library.filter(x => x.email === db.currentUser);
}

function userActivities() {
  return db.activities.filter(x => x.email === db.currentUser);
}

function logActivity(type, title, topic = "") {
  if (!db.currentUser) return;

  db.activities.unshift({
    id: Date.now() + Math.random(),
    email: db.currentUser,
    type,
    title,
    topic,
    date: new Date().toISOString()
  });

  saveDB();
  refreshDashboard();
}

/* AUTHENTICATION */

function showAuth(mode) {
  authMode = mode;
  const name = $("auth-name");
  if (name) name.style.display = mode === "register" ? "block" : "none";

  const form = $("auth-form");
  if (form) {
    const button = form.querySelector("button");
    if (button) button.textContent = mode === "register" ? "Create Account" : "Sign In";
  }

  if ($("auth-message")) $("auth-message").textContent = "";
}

if ($("auth-form")) {
  $("auth-form").addEventListener("submit", async e => {
    e.preventDefault();

    const name = $("auth-name").value.trim();
    const email = $("auth-email").value.trim().toLowerCase();
    const password = $("auth-password").value;

    if (!email || password.length < 6) {
      return authMessage("Enter a valid email and password of at least 6 characters.");
    }

    if (authMode === "register") {
      if (!name) return authMessage("Please enter your name.");
      if (db.users.some(u => u.email === email)) {
        return authMessage("This email already has an account.");
      }

      db.users.push({ name, email, password });
      db.currentUser = email;
      saveDB();
      openApp(true);
    } else {
      const user = db.users.find(u => u.email === email && u.password === password);

      if (!user) return authMessage("Email or password is incorrect.");

      db.currentUser = email;
      saveDB();
      openApp(false);
    }
  });
}

function authMessage(message) {
  if ($("auth-message")) $("auth-message").textContent = message;
}

function demoLogin() {
  let user = db.users.find(u => u.email === "demo@elvora.local");

  if (!user) {
    user = {
      name: "ELVORA Learner",
      email: "demo@elvora.local",
      password: "demo1234"
    };
    db.users.push(user);
  }

  db.currentUser = user.email;
  saveDB();
  openApp(true);
}

function forgotPassword() {
  const email = prompt("Enter your registered email:");
  if (!email) return;

  const user = db.users.find(u => u.email === email.trim().toLowerCase());

  if (!user) return toast("Account not found.");

  const newPassword = prompt("Enter a new password (at least 6 characters):");

  if (!newPassword || newPassword.length < 6) {
    return toast("Password must have at least 6 characters.");
  }

  user.password = newPassword;
  saveDB();
  toast("Local password updated.");
}

function logout() {
  db.currentUser = null;
  saveDB();
  stopWelcome();
  stopMusic();

  $("main-app").classList.add("hidden");
  $("welcome-screen").classList.add("hidden");
  $("auth-screen").classList.remove("hidden");
  showAuth("login");
}

/* APP START */

function openApp(firstTime = false) {
  $("auth-screen").classList.add("hidden");
  $("main-app").classList.remove("hidden");

  const user = currentUser();
  if (!user) return logout();

  if ($("student-name")) $("student-name").textContent = user.name;
  if ($("profile-name")) $("profile-name").value = user.name;

  refreshDashboard();
  refreshLibrary();
  refreshProgress();
  refreshFeedback();

  applySettings();

  if (firstTime) {
    startWelcome();
  } else {
    $("welcome-screen").classList.add("hidden");
    navigate("dashboard");
  }
}

/* NAVIGATION */

function toggleSidebar() {
  $("sidebar").classList.toggle("open");
}

function navigate(page) {
  const pages = document.querySelectorAll(".page");

  pages.forEach(p => p.classList.add("hidden"));

  const target = $("page-" + page);
  if (!target) return;

  target.classList.remove("hidden");
  $("sidebar").classList.remove("open");

  if (["smartnotes", "quizforge", "revision", "studycast"].includes(page)) {
    toast("Premium feature is locked. Preview only.");
  }

  if (page === "dashboard") refreshDashboard();
  if (page === "library") refreshLibrary();
  if (page === "progress") refreshProgress();
  if (page === "feedback") refreshFeedback();
}

/* WELCOME ANIMATION */

function startWelcome() {
  welcomeIndex = 0;
  $("main-app").classList.add("hidden");
  $("welcome-screen").classList.remove("hidden");
  renderWelcome();

  stopWelcome();

  if (!db.settings.reducedMotion) {
    welcomeTimer = setInterval(nextWelcome, 4400);
  }
}

function stopWelcome() {
  if (welcomeTimer) clearInterval(welcomeTimer);
  welcomeTimer = null;
}

function renderWelcome() {
  const slide = welcomeSlides[welcomeIndex];

  $("welcome-title").textContent = slide[0];
  $("welcome-description").textContent = slide[1];
  $("welcome-dots").textContent =
    welcomeSlides.map((_, i) => i === welcomeIndex ? "●" : "○").join("  ");
}

function nextWelcome() {
  welcomeIndex = (welcomeIndex + 1) % welcomeSlides.length;
  renderWelcome();
}

function previousWelcome() {
  welcomeIndex = (welcomeIndex - 1 + welcomeSlides.length) % welcomeSlides.length;
  renderWelcome();
}

function toggleWelcome() {
  if (welcomeTimer) {
    stopWelcome();
    toast("Welcome animation paused.");
  } else if (!db.settings.reducedMotion) {
    welcomeTimer = setInterval(nextWelcome, 4400);
    toast("Welcome animation resumed.");
  }
}

function finishWelcome() {
  stopWelcome();
  $("welcome-screen").classList.add("hidden");
  $("main-app").classList.remove("hidden");
  navigate("dashboard");
}

function skipWelcome() {
  finishWelcome();
}

function replayWelcome() {
  startWelcome();
}

/* DASHBOARD */

function refreshDashboard() {
  if (!db.currentUser) return;

  const activities = userActivities();
  const quizzes = db.quizzes.filter(q => q.email === db.currentUser);
  const learnt = db.cardsLearnt.filter(x => x.email === db.currentUser);

  setText("activity-count", activities.length);
  setText("quiz-count", quizzes.length);
  setText("cards-count", learnt.length);

  // Local browser account count only, not a global online learner count.
  setText("learner-count", db.users.length);

  const recent = $("recent-activities");

  if (recent) {
    recent.innerHTML = activities.slice(0, 5).map(a => `
      <article>
        <strong>${escapeHTML(a.title)}</strong>
        <p>${escapeHTML(a.type)} · ${formatDate(a.date)}</p>
      </article>
    `).join("") || "No activities yet.";
  }

  const user = currentUser();
  if (user) setText("student-name", user.name);
}

/* SMART MCQS */

function extractSentences(text) {
  return text
    .replace(/\s+/g, " ")
    .split(/(?<=[.!?])\s+/)
    .map(s => s.trim())
    .filter(s => s.length > 25);
}

function generateMCQs() {
  const source = $("mcq-source").value.trim();

  if (source.length < 50) {
    return toast("Please enter more study material.");
  }

  const sentences = extractSentences(source);
  const questions = [];

  for (const sentence of sentences) {
    const definitionMatch = sentence.match(
      /^(.{2,65}?)\s+(?:is defined as|is called|refers to|is known as)\s+(.{5,})$/i
    );

    if (definitionMatch) {
      questions.push({
        question: `What is ${definitionMatch[1].trim()}?`,
        answer: definitionMatch[2].trim(),
        explanation: sentence,
        category: "Definition"
      });
    } else if (/\d/.test(sentence) && /[=+\-×÷]/.test(sentence)) {
      questions.push({
        question: `Which statement matches this formula or fact: "${sentence}"?`,
        answer: sentence,
        explanation: sentence,
        category: "Formula"
      });
    } else {
      const words = sentence.split(/\s+/);
      const key = words.find(w => w.length > 6 && /^[a-zA-Z]+$/.test(w));

      if (key) {
        const hidden = sentence.replace(new RegExp(key, "i"), "______");
        questions.push({
          question: `Complete the statement: ${hidden}`,
          answer: key,
          explanation: sentence,
          category: "Recall"
        });
      }
    }
  }

  if (!questions.length) {
    return toast("Could not identify enough facts. Try clearer notes.");
  }

  const count = Math.min(Number($("mcq-count").value), 10);
  currentQuestions = questions.slice(0, count).map((q, i) => ({
    ...q,
    id: i,
    options: makeOptions(q.answer, sentences),
    selected: null
  }));

  renderMCQs();
  logActivity("MCQs", "Generated Smart MCQs");
}

function makeOptions(answer, sentences) {
  const options = [answer];

  const candidates = sentences
    .filter(s => s !== answer && s.length < 100)
    .map(s => s.replace(/[.!?]$/, "").trim())
    .filter(s => s.length > 3 && s.toLowerCase() !== answer.toLowerCase());

  for (const candidate of candidates) {
    if (options.length >= 4) break;
    if (!options.includes(candidate)) options.push(candidate);
  }

  while (options.length < 4) {
    options.push("Not stated in the supplied notes");
  }

  return options.sort(() => Math.random() - 0.5);
}

function renderMCQs() {
  $("mcq-results").innerHTML = currentQuestions.map((q, i) => `
    <article>
      <h3>Question ${i + 1}</h3>
      <p>${escapeHTML(q.question)}</p>
      ${q.options.map(option => `
        <label style="display:block;margin:10px 0">
          <input type="radio" name="mcq-${i}" value="${escapeHTML(option)}"
            onchange="selectMCQ(${i}, this.value)">
          ${escapeHTML(option)}
        </label>
      `).join("")}
    </article>
  `).join("");
}

function selectMCQ(index, answer) {
  currentQuestions[index].selected = answer;
}

function submitMCQs() {
  if (!currentQuestions.length) return toast("Generate questions first.");

  const correct = currentQuestions.filter(q =>
    q.selected && q.selected.trim().toLowerCase() === q.answer.trim().toLowerCase()
  ).length;

  const total = currentQuestions.length;

  const mistakes = currentQuestions
    .filter(q => !q.selected || q.selected.trim().toLowerCase() !== q.answer.trim().toLowerCase())
    .map(q => ({
      question: q.question,
      category: q.category,
      correctAnswer: q.answer,
      selected: q.selected || "Not answered"
    }));

  const result = {
    email: db.currentUser,
    correct,
    total,
    date: new Date().toISOString(),
    mistakes
  };

  db.quizzes.unshift(result);
  saveDB();

  $("mcq-diagnosis").innerHTML = `
    <article>
      <h2>Your Quiz Result</h2>
      <h3>${correct} / ${total}</h3>
      <p>Accuracy: ${Math.round(correct / total * 100)}%</p>
      <p>${mistakes.length} question(s) need revision.</p>
      ${mistakes.map(m => `
        <p><strong>${escapeHTML(m.category)}:</strong>
        ${escapeHTML(m.question)}<br>
        Correct answer: ${escapeHTML(m.correctAnswer)}</p>
      `).join("")}
    </article>
  `;

  logActivity("Quiz", "Completed Smart MCQs");
  refreshProgress();
}

/* FLIPCARDS */

function generateCards() {
  const source = $("cards-source").value.trim();
  if (source.length < 30) return toast("Please enter more study material.");

  const sentences = extractSentences(source);
  const count = Number($("cards-count").value);

  currentCards = sentences.slice(0, count).map((sentence, i) => ({
    id: Date.now() + i,
    front: `Remember this concept: ${i + 1}`,
    back: sentence,
    learnt: false
  }));

  currentCardIndex = 0;
  renderCard();
  logActivity("FlipCards", "Generated FlipCards");
}

function renderCard() {
  if (!currentCards.length) return;

  const card = currentCards[currentCardIndex];

  $("cards-results").innerHTML = `
    <article class="flip-card" onclick="flipCard(this)"
      style="min-height:180px;display:grid;place-items:center;text-align:center;cursor:pointer">
      <h3>${escapeHTML(card.front)}</h3>
      <p class="card-answer hidden">${escapeHTML(card.back)}</p>
      <small>Tap to flip</small>
    </article>
    <p>Card ${currentCardIndex + 1} of ${currentCards.length}</p>
    <button onclick="previousCard()">Previous</button>
    <button onclick="nextCard()">Next</button>
    <button onclick="markLearnt()">Mark as Learnt</button>
  `;
}

function flipCard(element) {
  const front = element.querySelector("h3");
  const back = element.querySelector(".card-answer");

  front.classList.toggle("hidden");
  back.classList.toggle("hidden");
  playCardTap();
}

function nextCard() {
  currentCardIndex = (currentCardIndex + 1) % currentCards.length;
  renderCard();
}

function previousCard() {
  currentCardIndex = (currentCardIndex - 1 + currentCards.length) % currentCards.length;
  renderCard();
}

function markLearnt() {
  const card = currentCards[currentCardIndex];

  if (!db.cardsLearnt.some(x => x.id === card.id && x.email === db.currentUser)) {
    db.cardsLearnt.push({ id: card.id, email: db.currentUser });
    card.learnt = true;
    saveDB();
    logActivity("Learning", "Learnt a FlipCard");
    toast("Card marked as learnt.");
  } else {
    toast("Already marked as learnt.");
  }
}

/* SMART SUMMARY */

function generateSummary() {
  const source = $("summary-source").value.trim();
  if (source.length < 50) return toast("Please enter more study material.");

  const sentences = extractSentences(source);
  const definitions = sentences.filter(s =>
    /is defined as|is called|refers to|is known as/i.test(s)
  );

  const formulas = source.match(/[^.!?]*\d[^.!?]*[=+\-×÷][^.!?]*/g) || [];

  currentSummary = `
    OVERVIEW
    ${sentences.slice(0, 2).join(" ")}

    KEY POINTS
    ${sentences.slice(0, 6).map(s => "• " + s).join("\n")}

    DEFINITIONS
    ${definitions.join("\n") || "No clear definitions detected."}

    FORMULAS
    ${formulas.join("\n") || "No clear formulas detected."}
  `;

  $("summary-results").innerHTML = `
    <article><h2>Smart Summary</h2>
    <pre style="white-space:pre-wrap;font-family:inherit">${escapeHTML(currentSummary)}</pre></article>
  `;

  logActivity("Summary", "Generated Smart Summary");
}

function saveSummary() {
  if (!currentSummary) return toast("Generate a summary first.");

  saveToLibrary("SMART_SUMMARY", "Smart Summary", currentSummary);
}

/* QUICK ANSWERS */

function generateAnswer() {
  const question = $("answer-question").value.trim();
  const source = $("answer-source").value.trim();

  if (!question || source.length < 20) {
    return toast("Enter a question and study material.");
  }

  const sentences = extractSentences(source);
  const keywords = question.toLowerCase().match(/[a-z]{3,}/g) || [];

  const ranked = sentences.map(sentence => {
    const text = sentence.toLowerCase();
    const score = keywords.reduce((sum, word) =>
      sum + (text.includes(word) ? 1 : 0), 0
    );
    return { sentence, score };
  }).sort((a, b) => b.score - a.score);

  const best = ranked[0];

  if (!best || best.score === 0) {
    $("answer-results").textContent =
      "I could not find enough supporting information in the supplied notes.";
  } else {
    $("answer-results").innerHTML = `
      <article>
        <h3>Answer</h3>
        <p>${escapeHTML(best.sentence)}</p>
        <small>Based on your supplied study material.</small>
      </article>
    `;
  }

  logActivity("Quick Answers", "Used Quick Answers");
}

/* LIBRARY */

function saveToLibrary(category, title, content) {
  db.library.unshift({
    id: Date.now(),
    email: db.currentUser,
    category,
    title,
    topic: "",
    content,
    date: new Date().toISOString()
  });

  saveDB();
  refreshLibrary();
  logActivity("Library", "Saved to Library");
  toast("Saved to your Library.");
}

function refreshLibrary() {
  const container = $("library-items");
  if (!container) return;

  const search = ($("library-search")?.value || "").toLowerCase();
  const filter = $("library-filter")?.value || "ALL";

  const items = userItems().filter(item => {
    const matchesSearch = (item.title + item.content).toLowerCase().includes(search);
    const matchesCategory = filter === "ALL" || item.category === filter;
    return matchesSearch && matchesCategory;
  });

  container.innerHTML = items.map(item => `
    <article>
      <strong>${escapeHTML(item.title)}</strong>
      <p>${escapeHTML(item.category)} · ${formatDate(item.date)}</p>
      <button onclick="openLibraryItem(${item.id})">Open</button>
      <button onclick="editLibraryItem(${item.id})">Edit</button>
      <button onclick="deleteLibraryItem(${item.id})">Delete</button>
    </article>
  `).join("") || "<p>Your library is empty.</p>";
}

function openLibraryItem(id) {
  const item = userItems().find(x => x.id === id);
  if (!item) return;

  alert(item.title + "\n\n" + item.content.slice(0, 900));
}

function editLibraryItem(id) {
  const item = userItems().find(x => x.id === id);
  if (!item) return;

  const title = prompt("Edit title:", item.title);
  if (title === null || !title.trim()) return;

  item.title = title.trim();
  saveDB();
  refreshLibrary();
}

function deleteLibraryItem(id) {
  if (!confirm("Delete this saved item?")) return;

  db.library = db.library.filter(x => !(x.id === id && x.email === db.currentUser));
  saveDB();
  refreshLibrary();
}

if ($("library-search")) $("library-search").addEventListener("input", refreshLibrary);
if ($("library-filter")) $("library-filter").addEventListener("change", refreshLibrary);

/* PROGRESS */

function refreshProgress() {
  if (!db.currentUser) return;

  const activities = userActivities();
  const quizzes = db.quizzes.filter(q => q.email === db.currentUser);
  const learnt = db.cardsLearnt.filter(x => x.email === db.currentUser);

  const totalCorrect = quizzes.reduce((n, q) => n + q.correct, 0);
  const totalQuestions = quizzes.reduce((n, q) => n + q.total, 0);
  const accuracy = totalQuestions ? Math.round(totalCorrect / totalQuestions * 100) : 0;

  $("progress-metrics").innerHTML = `
    <article><h3>${activities.length}</h3><p>Total Activities</p></article>
    <article><h3>${accuracy}%</h3><p>Quiz Accuracy</p></article>
    <article><h3>${learnt.length}</h3><p>Cards Learnt</p></article>
    <article><h3>${quizzes.length}</h3><p>Quizzes Completed</p></article>
  `;

  const days = Array.from({ length: 7 }, (_, i) => {
    const date = new Date();
    date.setDate(date.getDate() - (6 - i));
    const key = date.toDateString();

    return {
      label: date.toLocaleDateString(undefined, { weekday: "short" }),
      count: activities.filter(a => new Date(a.date).toDateString() === key).length
    };
  });

  $("weekly-chart").innerHTML = days.map(d => `
    <article>
      <p>${d.label}</p>
      <div style="height:8px;background:#263766;border-radius:20px;overflow:hidden">
        <div style="width:${Math.min(d.count * 12, 100)}%;height:100%;background:linear-gradient(90deg,#4d8dff,#e83e91)"></div>
      </div>
      <small>${d.count} activities</small>
    </article>
  `).join("");

  $("topic-performance").innerHTML =
    quizzes.slice(0, 6).map(q => `
      <article>${q.correct}/${q.total} correct · ${formatDate(q.date)}</article>
    `).join("") || "Complete a quiz to see your performance.";

  $("weak-topics").textContent =
    quizzes.some(q => q.mistakes.length)
      ? "Review the incorrect answers in your quiz results."
      : "No weak areas recorded yet.";

  $("quiz-history").innerHTML = quizzes.slice(0, 6).map(q => `
    <article>${q.correct}/${q.total} · ${formatDate(q.date)}</article>
  `).join("") || "No quiz attempts yet.";
}

/* FEEDBACK */

function submitFeedback() {
  const category = $("feedback-category").value;
  const rating = Number($("feedback-rating").value);
  const liked = $("feedback-liked").value.trim();
  const improve = $("feedback-improve").value.trim();

  if (!liked && !improve) return toast("Please write your feedback.");

  db.feedback.unshift({
    email: db.currentUser,
    category,
    rating,
    liked,
    improve,
    date: new Date().toISOString()
  });

  saveDB();

  $("feedback-liked").value = "";
  $("feedback-improve").value = "";

  refreshFeedback();
  logActivity("Feedback", "Submitted Feedback");
  toast("Thank you for your feedback!");
}

function refreshFeedback() {
  const box = $("feedback-history");
  if (!box) return;

  box.innerHTML = db.feedback
    .filter(f => f.email === db.currentUser)
    .map(f => `
      <article>
        <strong>${escapeHTML(f.category)} · ${"★".repeat(f.rating)}</strong>
        <p>${escapeHTML(f.liked)}</p>
        <p>${escapeHTML(f.improve)}</p>
      </article>
    `).join("") || "No feedback submitted yet.";
}

/* PROFILE */

function saveProfile() {
  const user = currentUser();
  if (!user) return;

  const name = $("profile-name").value.trim();
  if (!name) return toast("Enter a name.");

  user.name = name;
  saveDB();
  refreshDashboard();
  toast("Profile updated.");
}

/* SETTINGS */

function applySettings() {
  document.body.classList.toggle("light", db.settings.theme === "light");
  document.body.classList.toggle("reduce-motion", db.settings.reducedMotion);

  if ($("theme-setting")) $("theme-setting").value = db.settings.theme;
  if ($("music-setting")) $("music-setting").checked = db.settings.music;
  if ($("music-volume")) $("music-volume").value = db.settings.volume;
  if ($("motion-setting")) $("motion-setting").checked = db.settings.reducedMotion;
  if ($("reminder-setting")) $("reminder-setting").checked = db.settings.reminders;
}

function changeTheme(theme) {
  db.settings.theme = theme;

  if (theme === "system") {
    document.body.classList.toggle(
      "light",
      window.matchMedia("(prefers-color-scheme: light)").matches
    );
  } else {
    document.body.classList.toggle("light", theme === "light");
  }

  saveDB();
}

if ($("music-setting")) {
  $("music-setting").addEventListener("change", e => {
    db.settings.music = e.target.checked;
    saveDB();
    if (e.target.checked) startMusic();
    else stopMusic();
  });
}

if ($("music-volume")) {
  $("music-volume").addEventListener("input", e => {
    db.settings.volume = Number(e.target.value);
    saveDB();
  });
}

if ($("motion-setting")) {
  $("motion-setting").addEventListener("change", e => {
    db.settings.reducedMotion = e.target.checked;
    saveDB();
    applySettings();
  });
}

if ($("reminder-setting")) {
  $("reminder-setting").addEventListener("change", e => {
    db.settings.reminders = e.target.checked;
    saveDB();
  });
}

function clearStudyData() {
  if (!confirm("Clear your study history and saved library?")) return;

  db.library = db.library.filter(x => x.email !== db.currentUser);
  db.activities = db.activities.filter(x => x.email !== db.currentUser);
  db.quizzes = db.quizzes.filter(x => x.email !== db.currentUser);
  db.cardsLearnt = db.cardsLearnt.filter(x => x.email !== db.currentUser);

  saveDB();
  refreshDashboard();
  refreshLibrary();
  refreshProgress();
  toast("Local study data cleared.");
}

/* AUDIO: ORIGINAL BROWSER SYNTHESIS */

function toggleMusic() {
  if (musicPlaying) stopMusic();
  else startMusic();
}

function startMusic() {
  try {
    const AudioCtx = window.AudioContext || window.webkitAudioContext;
    if (!AudioCtx) return toast("Audio is not supported in this browser.");

    musicContext = musicContext || new AudioCtx();
    musicContext.resume();

    if (musicPlaying) return;

    musicPlaying = true;
    const master = musicContext.createGain();
    master.gain.value = (db.settings.volume / 100) * 0.035;
    master.connect(musicContext.destination);

    const notes = [130.81, 164.81, 196, 164.81, 146.83, 174.61, 220, 174.61];
    let step = 0;

    function playNote() {
      if (!musicPlaying) return;

      const osc = musicContext.createOscillator();
      const gain = musicContext.createGain();

      osc.type = "triangle";
      osc.frequency.value = notes[step % notes.length];

      gain.gain.setValueAtTime(0.001, musicContext.currentTime);
      gain.gain.linearRampToValueAtTime(0.4, musicContext.currentTime + 0.08);
      gain.gain.exponentialRampToValueAtTime(0.001, musicContext.currentTime + 0.8);

      osc.connect(gain);
      gain.connect(master);
      osc.start();
      osc.stop(musicContext.currentTime + 0.85);

      step++;
      musicOscillators.push(setTimeout(playNote, 650));
    }

    playNote();
    $("music-button").textContent = "♫ ON";
  } catch {
    toast("Could not start browser audio.");
  }
}

function stopMusic() {
  musicPlaying = false;
  musicOscillators.forEach(clearTimeout);
  musicOscillators = [];
  $("music-button").textContent = "♫";
}

function playCardTap() {
  try {
    const AudioCtx = window.AudioContext || window.webkitAudioContext;
    const ctx = new AudioCtx();
    const osc = ctx.createOscillator();
    const gain = ctx.createGain();

    osc.type = "sine";
    osc.frequency.setValueAtTime(320, ctx.currentTime);
    osc.frequency.exponentialRampToValueAtTime(110, ctx.currentTime + 0.055);

    gain.gain.setValueAtTime(0.08, ctx.currentTime);
    gain.gain.exponentialRampToValueAtTime(0.001, ctx.currentTime + 0.055);

    osc.connect(gain);
    gain.connect(ctx.destination);
    osc.start();
    osc.stop(ctx.currentTime + 0.06);
    osc.onended = () => ctx.close();
  } catch {}
}

/* PREMIUM PREVIEW PLACEHOLDERS */

function toast(message) {
  const box = $("toast");
  if (!box) return;

  box.textContent = message;
  box.classList.add("show");

  setTimeout(() => box.classList.remove("show"), 2400);
}

function setText(id, value) {
  if ($(id)) $(id).textContent = value;
}

/* HELPERS */

function escapeHTML(value) {
  return String(value ?? "").replace(/[&<>"']/g, char => ({
    "&": "&amp;",
    "<": "&lt;",
    ">": "&gt;",
    '"': "&quot;",
    "'": "&#039;"
  })[char]);
}

function formatDate(date) {
  return new Date(date).toLocaleDateString();
}

/* STARTUP */

if ($("library-search")) $("library-search").addEventListener("input", refreshLibrary);

if (db.currentUser && currentUser()) {
  openApp(false);
} else {
  $("auth-screen").classList.remove("hidden");
  $("main-app").classList.add("hidden");
  $("welcome-screen").classList.add("hidden");
  showAuth("login");
}
