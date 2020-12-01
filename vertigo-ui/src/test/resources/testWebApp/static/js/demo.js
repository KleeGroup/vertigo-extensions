
quasarConfig = {
    loadingBar: { 'skip-hijack' : true } // disable quasar's ajaxbar
}

/******
 * 
 * @returns
 */
function configDatePickers(){
        $(".dateSelector").datepicker({
                                                todayBtn: "linked",
                                                clearBtn: true,
                                                language: "fr"
        });
}

/**
 *  CORRECTIF permettant de mofidier les datepickers avec l'autocompletion du navigateur
 *  Prend en compte tous les inputs avec un id commençant par "datepicker"
 */
function patchDatePicker(){
        let listeDatepicker = $('input[id^="datepicker"]');
        listeDatepicker.each(function(idx){
                let datepicker = $(this);               
                datepicker.select(function(e){                  
                        datepicker.datepicker("update", datepicker.val());      
                });
        });
}