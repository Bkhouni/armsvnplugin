function checkGotoForm(submitButton) {  
    var form = submitButton.form;
    if (!form.setRevision[0].checked) {
        if (!isNumber("inputRevision")) {
            alert('Revision count must be numeric and greater then 0'); 
            return false;
        }                
    }
    return true;
}

function disableField(field) {
    var form = field.form;
    form.elements["inputRevision"].disabled = true;        
}

function enableField(field) {
    var form = field.form;
    form.elements["inputRevision"].disabled = false;        
}

function isNumber(fieldName) {
    var re = new RegExp('[0-9.]'); 
    return !(!re.test(document.getElementById(fieldName).value) || document.getElementById(fieldName).value <= 0);
}

function checkRevisionNumber() {
    if (isNumber("revcount")) {
        return true;    
    } else {
        alert('Revision count must be numeric and greater then 0'); 
        return false;
    }
}

function checkRevisionsRange() {
   if (isNumber("startrange") && isNumber("endrange")) {
       return true;     
   } else {
       alert('Revision count must be numeric and greater then 0'); 
       return false;
   } 
}

function navigate(url) {        
    if (checkRevisionNumber() == true) {
        window.location = url + "&revcount=" + document.getElementById('revcount').value;
    } else   {
        return false;
    }  
}

function rangeNavigate(url) {        
    if (checkRevisionsRange() == true) {    
        window.location = url + "&startrevision=" + document.getElementById('startrange').value + "&endrevision=" + document.getElementById('endrange').value;
    } else   {
        return false;
    }          
}