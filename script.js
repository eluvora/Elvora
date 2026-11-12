const $ = (id) => document.getElementById(id);
const store = {
  get(key, fallback=[]) { try { return JSON.parse(localStorage.getItem("elvora_"+key)) ?? fallback; } catch { return fallback; } },
  set(key, value) { localStorage.setItem("elvora_"+key, JSON.stringify(value)); }
};
const pageNames = {dashboard:"Dashboard",smartnotes:"SmartNotes",quizforge:"QuizForge",flashcards:"Flashcards",summary:"Smart Summary",answers:"Quick Answers",library:"My Library",revision:"Last Minute Revision",studycast:"StudyCast",progress:"Progress",profile:"My Profile",settings:"Settings",help:"Help & Support"};
function go(page) {
  if (!pageNames[page]) return;
  document.querySelectorAll(".page").forEach(p=>p.classList.toggle("active",p.id===page));
  document.querySelectorAll(".nav-btn").forEach(b=>b.classList.toggle("active",b.dataset.page===page));
  $("pageTitle").textContent=pageNames[page];
  $("sidebar").classList.remove("open");
  if(page==="progress") updateStats();
  window.scrollTo({top:0,behavior:"smooth"});
}
document.addEventListener("click",e=>{
  const target=e.target.closest("[data-page]");
  if(target){e.preventDefault();go(target.dataset.page);}
});
$("mobileMenu").addEventListener("click",()=>$("sidebar").classList.toggle("open"));
$("themeToggle").addEventListener("click",toggleTheme);
$("settingsTheme").addEventListener("click",toggleTheme);
function toggleTheme(){document.body.classList.toggle("dark");localStorage.setItem("elvora_dark",document.body.classList.contains("dark"));}
if(localStorage.getItem("elvora_dark")==="true")document.body.classList.add("dark");

function renderItems(key,container,titleKey,bodyKey){
  const items=store.get(key); const el=$(container); el.innerHTML="";
  if(!items.length){el.innerHTML='<p class="empty-state">Nothing saved here yet.</p>';return;}
  items.forEach((item,i)=>{
    const div=document.createElement("div");div.className="saved-item";
    const content=document.createElement("div");
    const h=document.createElement("h3");h.textContent=item[titleKey]||"Untitled";
    const p=document.createElement("p");p.textContent=item[bodyKey]||"";
    content.append(h,p);
    if(item.url){const a=document.createElement("a");a.href=item.url;a.target="_blank";a.rel="noopener";a.textContent="Open resource";content.append(a);}
    const del=document.createElement("button");del.className="delete-btn";del.textContent="Delete";
    del.addEventListener("click",()=>{items.splice(i,1);store.set(key,items);renderAll();});
    div.append(content,del);el.append(div);
  });
}
function renderAll(){
 renderItems("notes","notesList","title","body");
 renderItems("answers","answersList","title","body");
 renderItems("resources","libraryList","title","url");
 renderItems("revisions","revisionList","title","body");
 renderQuiz();renderFlashcards();updateStats();
}
$("saveNote").addEventListener("click",()=>{
 const title=$("noteTitle").value.trim(),body=$("noteContent").value.trim();
 if(!title||!body){alert("Please enter a title and note.");return;}
 const items=store.get("notes");items.unshift({title,body});store.set("notes",items);
 $("noteTitle").value="";$("noteContent").value="";renderAll();
});
$("clearNote").addEventListener("click",()=>{$("noteTitle").value="";$("noteContent").value="";});

$("addQuestion").addEventListener("click",()=>{
 const question=$("quizQuestion").value.trim();
 const options=["optionA","optionB","optionC","optionD"].map(id=>$(id).value.trim());
 if(!question||options.some(x=>!x)){alert("Please complete the question and all four options.");return;}
 const items=store.get("questions");items.push({question,options,correct:Number($("correctOption").value)});store.set("questions",items);
 ["quizQuestion","optionA","optionB","optionC","optionD"].forEach(id=>$(id).value="");renderAll();
});
function renderQuiz(){
 const el=$("quizArea"),items=store.get("questions");el.innerHTML="";
 if(!items.length){el.innerHTML='<p class="empty-state">Add your first question above.</p>';return;}
 items.forEach((q,i)=>{const d=document.createElement("div");d.className="saved-item";const c=document.createElement("div");const h=document.createElement("h3");h.textContent=(i+1)+". "+q.question;const p=document.createElement("p");p.textContent=q.options.map((o,j)=>String.fromCharCode(65+j)+". "+o).join("  ·  ");c.append(h,p);const del=document.createElement("button");del.className="delete-btn";del.textContent="Delete";del.onclick=()=>{items.splice(i,1);store.set("questions",items);renderAll();};d.append(c,del);el.append(d);});
}
$("startQuiz").addEventListener("click",()=>{
 const items=store.get("questions");if(!items.length){alert("Add some questions first.");return;}
 let score=0;
 items.forEach((q,i)=>{const answer=prompt((i+1)+". "+q.question+"\n"+q.options.map((o,j)=>String.fromCharCode(65+j)+". "+o).join("\n")+"\n\nEnter A, B, C or D:");if(answer&&"ABCD".indexOf(answer.trim().toUpperCase())===q.correct)score++;});
 alert("Quiz complete! Your score: "+score+" / "+items.length);
});
$("addFlashcard").addEventListener("click",()=>{
 const front=$("flashQuestion").value.trim(),back=$("flashAnswer").value.trim();
 if(!front||!back){alert("Enter both the front and back.");return;}
 const items=store.get("flashcards");items.unshift({front,back});store.set("flashcards",items);$("flashQuestion").value="";$("flashAnswer").value="";renderAll();
});
function renderFlashcards(){
 const el=$("flashcardList"),items=store.get("flashcards");el.innerHTML="";
 if(!items.length){el.innerHTML='<p class="empty-state">Your flashcards will appear here.</p>';return;}
 items.forEach((card,i)=>{const d=document.createElement("button");d.className="flashcard";d.type="button";let flipped=false;const strong=document.createElement("strong"),small=document.createElement("small");strong.textContent=card.front;small.textContent="Tap to reveal answer";d.append(strong,small);d.onclick=()=>{flipped=!flipped;strong.textContent=flipped?card.back:card.front;small.textContent=flipped?"Answer":"Tap to reveal answer";};el.append(d);});
}
$("makeSummary").addEventListener("click",()=>{
 const text=$("summaryInput").value.trim();if(!text){alert("Paste some text first.");return;}
 const sentences=text.replace(/\s+/g," ").split(/(?<=[.!?])\s+/).filter(Boolean);
 const words=text.split(/\s+/).filter(Boolean);
 const bullets=sentences.map(s=>"• "+s.trim()).join("\n");
 $("summaryOutput").textContent="STUDY OUTLINE\n\n"+bullets+"\n\nWord count: "+words.length+"\nSentence count: "+sentences.length;
});
$("saveAnswer").addEventListener("click",()=>{
 const title=$("answerTitle").value.trim(),body=$("answerText").value.trim();if(!title||!body){alert("Enter a topic and answer.");return;}
 const a=store.get("answers");a.unshift({title,body});store.set("answers",a);$("answerTitle").value="";$("answerText").value="";renderAll();
});
$("addResource").addEventListener("click",()=>{
 const title=$("resourceName").value.trim(),url=$("resourceUrl").value.trim();
 if(!title||!url){alert("Enter a resource name and URL.");return;}
 try{const parsed=new URL(url);if(!["http:","https:"].includes(parsed.protocol))throw Error();const a=store.get("resources");a.unshift({title,url:parsed.href});store.set("resources",a);$("resourceName").value="";$("resourceUrl").value="";renderAll();}catch{alert("Please enter a valid http or https URL.");}
});
$("saveRevision").addEventListener("click",()=>{
 const title=$("revisionTitle").value.trim(),body=$("revisionText").value.trim();if(!title||!body){alert("Enter a chapter and revision notes.");return;}
 const a=store.get("revisions");a.unshift({title,body});store.set("revisions",a);$("revisionTitle").value="";$("revisionText").value="";renderAll();
});
function updateStats(){$("statNotes").textContent=store.get("notes").length;$("statQuestions").textContent=store.get("questions").length;$("statCards").textContent=store.get("flashcards").length;$("statAnswers").textContent=store.get("answers").length;}
$("saveProfile").addEventListener("click",()=>{const name=$("profileName").value.trim();if(!name){alert("Enter a name first.");return;}localStorage.setItem("elvora_profile",name);$("profileMessage").textContent="Profile name saved on this device.";});
$("profileName").value=localStorage.getItem("elvora_profile")||"";
$("clearData").addEventListener("click",()=>{if(confirm("Delete all ELVORA data saved in this browser? This cannot be undone.")){Object.keys(localStorage).filter(k=>k.startsWith("elvora_")&&!["elvora_dark","elvora_profile"].includes(k)).forEach(k=>localStorage.removeItem(k));renderAll();alert("Saved study data cleared.");}});
let timer=25*60,interval=null;
function showTimer(){$("timerDisplay").textContent=String(Math.floor(timer/60)).padStart(2,"0")+":"+String(timer%60).padStart(2,"0");}
$("timerStart").addEventListener("click",()=>{if(interval)return;interval=setInterval(()=>{if(timer>0){timer--;showTimer();}else{clearInterval(interval);interval=null;alert("Focus session complete. Take a short break!");}},1000);});
$("timerPause").addEventListener("click",()=>{clearInterval(interval);interval=null;});
$("timerReset").addEventListener("click",()=>{clearInterval(interval);interval=null;timer=25*60;showTimer();});
$("searchButton").addEventListener("click",()=>{const q=prompt("Which tool would you like to open?");if(!q)return;const match=Object.keys(pageNames).find(k=>pageNames[k].toLowerCase().includes(q.toLowerCase())||k.includes(q.toLowerCase()));if(match)go(match);else alert("No matching page found.");});
showTimer();renderAll();
