// Initialize Firebase
var config = {
  apiKey: 'AIzaSyBS-GQ8ytFxnYnmkU8rh-aCnd5GSPyd3bY',
  databaseURL: 'https://passwallet-afa54.firebaseio.com',
  storageBucket: 'passwallet-afa54.appspot.com'
};
firebase.initializeApp(config);
var username;
var check;
var site;
var key ;
var iv;
var crypt;
var abc;
var docref;
var on = false;


function exists(number){
   firebase.database().ref('/extensions/' + number).once('value').then(function(snapshot) {
     return snapshot.val() != null;
  });
}

function initApp() {
  firebase.auth().onAuthStateChanged(function(user) {
    if(firebase.auth().currentUser !== null) {
      var user2 = firebase.auth().currentUser;
      var uid = user2.uid;
      if (user2) {
        var dname = user.displayName;
        if (dname === null) {
          dname = LeftPadWithZeros(Math.floor((Math.random() * 100000) + 1), 6);
          if(exists(dname)){
            dname = LeftPadWithZeros(Math.floor((Math.random() * 100000) + 1), 6);
          }
        }
        writeUserData(uid, dname, crypt.getPublicKey());
        user.updateProfile({
          displayName: dname,
        }).then(function () {
        }).catch(function (error) {
        });

        document.getElementById('extid').textContent = dname;

         docref = firebase.database().ref('extensions/' + dname + "/appk");
        docref.on('value', function (data) {
          if (data.val() !== null) {
            document.getElementById('2nd').style.display = "block";

            var enckey = rsaenc(key, data.val());

            var text = encodeURIComponent(enckey);

            var url = "http://bwipjs-api.metafloor.com/?bcid=qrcode&text=" + text + "&eclevel=M&scale=2";

            document.getElementById('code').src = url;

            abc = firebase.database().ref('extensions/' + dname);
            abc.on('value', function (data) {
              if (data.val().spass !== undefined) {
                on = true;
                document.getElementById('3rd').style.display = "block";
                document.getElementById("sname").innerText = data.val().sname;
                document.getElementById('fill').addEventListener('click', function () {
                  site = data.val().sname.toLowerCase();
                  var rsat1 = rsadenc(data.val().suname, data.val().spass, crypt.getPrivateKey());
                  try{
                    aesdenc(rsat1.text1, rsat1.text2, data.val().iv, key);
                  }catch (e) {
                    abort();
                  }
                }, false);
              }
            });
          }
        });
      } else {
        document.getElementById('extid').textContent = 'None';
      }
      document.getElementById('quickstart-button').disabled = false;
    }});
  document.getElementById('quickstart-button').addEventListener('click', refresh, false);
}

function sendMessage(uname,pass) {
  let params = {
    active:true,
    currentWindow:true
  };

  chrome.tabs.query(params,gotTabs);

  function gotTabs(tabs){
    let msg = {
      title: "credentials",
      uname: uname,
      pass: pass,
      site:site
    };
    chrome.tabs.sendMessage(tabs[0].id,msg);
  }
}

/**
 * Start the auth flow and authorizes to Firebase.
 * @param{boolean} interactive True if the OAuth flow should request with an interactive mode.
 */
function startAuth(interactive) {
  firebase.auth().signInAnonymously().catch(function(error) {
    var errorCode = error.code;
    var errorMessage = error.message;
    console.log(errorMessage);
  });

}

function LeftPadWithZeros(number, length)
{
  var str = '' + number;
  while (str.length < length) {
    str = '0' + str;
  }

  return str;
}

function refresh() {
  if (firebase.auth().currentUser) {
    var user = firebase.auth().currentUser;
    deleteData(user.displayName);
    firebase.auth().signOut();
    user.delete().then(function() {
      // User deleted
      if(on){
        window.close();
      }

    }).catch(function(error) {
      // An error happened.
    });

    if(!on) {
      document.getElementById('2nd').style.display = "none";
      document.getElementById('3rd').style.display = "none";
      key = createRandomWord(16);
      generatekeys();
      iv = secureRandom(16);
      startAuth(true);
    }

  } else {
    key =  createRandomWord(16);
    generatekeys();
    iv = secureRandom(16);
    startAuth(true);
  }
}

function generatekeys() {
  var sKeySize = "1024";
  var keySize = parseInt(sKeySize);
  crypt = new JSEncrypt({ default_key_size: keySize });
}

function rsaenc(text,key) {
  var encrypt = new JSEncrypt();
  encrypt.setPublicKey(key);
  return encrypt.encrypt(text);
}
function rsadenc(text,text2,key) {
  var dencrypt = new JSEncrypt();
  dencrypt.setPrivateKey(key);
  var detext1 = dencrypt.decrypt(text);
  var detext2 = dencrypt.decrypt(text2);
  return {text1:detext1,
    text2:detext2};
}

function aesdenc(text,pass,siv,spass){

  var iterationCount = 1000;
  var keySize = 128/32;
  var encryptionKey  = spass;
  var dataToDecrypt = text;
  var iv = siv;
  var salt = siv;


  var key = CryptoJS.PBKDF2(
      encryptionKey,
      CryptoJS.enc.Hex.parse(salt),
      { keySize: keySize, iterations: iterationCount });
  var ciphertext = dataToDecrypt.toString(CryptoJS.enc.Base64);
  var cipherParams = CryptoJS.lib.CipherParams.create({
    ciphertext: CryptoJS.enc.Base64.parse(ciphertext)
  });
  var decrypted = CryptoJS.AES.decrypt(
      cipherParams,
      key,
      { iv: CryptoJS.enc.Hex.parse(iv) });
  dataToDecrypt =  pass;
  key = CryptoJS.PBKDF2(
      encryptionKey,
      CryptoJS.enc.Hex.parse(salt),
      { keySize: keySize, iterations: iterationCount });
  ciphertext = dataToDecrypt.toString(CryptoJS.enc.Base64);
  cipherParams = CryptoJS.lib.CipherParams.create({
    ciphertext: CryptoJS.enc.Base64.parse(ciphertext)
  });
  var decrypted2 = CryptoJS.AES.decrypt(
      cipherParams,
      key,
      { iv: CryptoJS.enc.Hex.parse(iv) });

  var pl = decrypted.toString(CryptoJS.enc.Utf8);
  var pl2 = decrypted2.toString(CryptoJS.enc.Utf8);


  sendMessage(pl,pl2);

  refresh();
}

function abort() {
  alert("Something went wrong! Please refresh the page and try again.");
  refresh();
  window.close();
}

function writeUserData(userId, name,extpk) {
  var docref = firebase.database().ref('extensions/' + name);
  docref.once('value', function(data) {
    if(data.val()=== null) {
      firebase.database().ref('extensions/' + name).set({
        uid: userId,
        extpk:extpk
      });
    } });

}

function deleteData(name) {
  var release = firebase.database().ref('extensions/' + name);
  release.once('value', function(data) {
    firebase.database().ref('extensions/' + name+"/done").set("true");
    firebase.database().ref('extensions/' + name+"/uid").set(null);
    firebase.database().ref('extensions/' + name+"/appid").set(null);
    firebase.database().ref('extensions/' + name+"/suname").set(null);
    firebase.database().ref('extensions/' + name+"/spass").set(null);
    firebase.database().ref('extensions/' + name+"/iv").set(null);
    firebase.database().ref('extensions/' + name+"/appk").set(null);
    firebase.database().ref('extensions/' + name+"/extpk").set(null);
    firebase.database().ref('extensions/' + name+"/sname").set(null);
    firebase.database().ref('extensions/' + name+"/done").set(null);
    if(on){
      abc.off();
      docref.off();
    }
  });
}

function createRandomWord(length) {
  var consonants = 'bcdfghjlmnpqrstv',
      vowels = 'aeiou',
      rand = function(limit) {
        return Math.floor(Math.random()*limit);
      },
      i, word='', length = parseInt(length,10),
      consonants = consonants.split(''),
      vowels = vowels.split('');
  for (i=0;i<length/2;i++) {
    var randConsonant = consonants[rand(consonants.length)],
        randVowel = vowels[rand(vowels.length)];
    word += (i===0) ? randConsonant.toUpperCase() : randConsonant;
    word += i*2<length-1 ? randVowel : '';
  }
  return word;
}

window.onload = function() {
  key = createRandomWord(16);
  iv = secureRandom(16);
  generatekeys();

  initApp();
  startAuth(true);
  document.getElementById('2nd').style.display="none";
  document.getElementById('3rd').style.display="none";
};
