
chrome.runtime.onMessage.addListener(gotMessage);

function gotMessage(message,sender,sendResponse) {
    if(message.title === "credentials") {
        var inputs = document.getElementsByTagName('input');
        var counter = 0;
        var used = false;
        var url = window.location.href;

        if (url.includes(message.site)) {
            for (var i = 0; i < inputs.length; i++) {
                switch (inputs[i].type.toLowerCase()) {
                    case 'email':
                        if (counter < 2) {
                            if(!used){
                            inputs[i].value = message.uname;
                            counter++;
                            used = true;
                            }
                        }
                        break;
                    case 'text':
                        if (counter < 2) {
                            if(!used){
                                inputs[i].value = message.uname;
                                counter++;
                                used = true;
                            }
                        }
                        break;
                    case 'password':
                        if (counter < 2) {
                            inputs[i].value = message.pass;
                            counter++;
                        }
                        break;
                }
            }
        }else {
            alert("Credentials provided do not belong to this website");
        }
        }
    if(message.title === "error"){
    }
}