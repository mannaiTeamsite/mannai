
/*************************************Calendar js******************************************************************/
// $(document).ready(function(){
    function ev_calendar(){
        $('#demo').click();
        // debugger
        $('.calendar_wrapper').find('.today').parent().siblings().hide();
        $('.calendar_wrapper').find('td').css('pointer-events', 'none');

        // $(document).on('DOMNodeInserted', function(e) {
        //     $(this).find('.today').parent().siblings().hide();
        //     $(this).find('td').css('pointer-events', 'none');
        // });

        $(document).on('click', '.view_all', function(){
            console.log('click');
            if(!$(this).hasClass('min_view')){
                $(this).addClass('min_view');
                $(this).parent().addClass('full_view');
                $(this).parents('.calendar_wrapper').find('.today').parent().siblings().show();
                $(this).parents('.calendar_wrapper').find('td').css('pointer-events', 'auto');
            }
            else{
                $(this).removeClass('min_view');
                $(this).parent().removeClass('full_view');
                $(this).parents('.calendar_wrapper').find('.today').parent().siblings().hide();
                $(this).parents('.calendar_wrapper').find('td').css('pointer-events', 'none');
            }
        });
    }
   
// });
    // ======= show current week only =======
    // $(document).on('find', 'td').on('click', function(e){
    //    console.log('hello')
    //    e.preventDefault();
    //    e.stopPropagation();
    
    // })
    // ======= show current week only =======
    //   ======= get current date =======
    var today = new Date();
    var dd = String(today.getDate()).slice(-2);
    var mm = String(today.getMonth() + 1).slice(-2); //January is 0!
    var yyyy = today.getFullYear();
    
    today = mm + '/' + dd + '/' + yyyy;
    //   ======= get current date =======
    if (window.matchMedia("(max-width: 600px)").matches) {
        if($('body').hasClass('ar')){
            initCalanderAr() //calander initialize on mobile device 
        }
        else{
            initCalanderMinView() //calander initialize on mobile device 
        }
        
    } else {
        if($('body').hasClass('ar')){
            initCalanderAr()  //calander initialize on desktop device
        }
        else{
            initCalander()  //calander initialize on desktop device
        }
        
    }
 
    //calendar initialization for dekstop in endlish
 function initCalander(){
    $('#demo').daterangepicker({
        "parentEl": ".events",
        "singleDatePicker": true,
        "showDropdowns": true,
        "autoApply": true,
        "locale": {
            "format": "MM/DD/YYYY",
            "separator": " - ",
            "applyLabel": "Apply",
            "cancelLabel": "Cancel",
            "fromLabel": "From",
            "toLabel": "To",
            "customRangeLabel": "Custom",
            "weekLabel": "W",
            "daysOfWeek": [
                "Sunday",
                "Monday",
                "Tuesday",
                "Wednesday",
                "Thursday",
                "Friday ",
                "Saturday"
            ],
            "monthNames": [
                "January",
                "February",
                "March",
                "April",
                "May",
                "June",
                "July",
                "August",
                "September",
                "October",
                "November",
                "December"
            ],
            "firstDay": 1
        },
        "linkedCalendars": false,
        "showCustomRangeLabel": false,
        "alwaysShowCalendars": true,
        "startDate": today,
         "endDate": "12/12/2020",
        isInvalidDate: function(ele) {
          // console.log(ele);
        var currDate = moment(ele._d).format('YY-MM-DD');
        
        return ["20-12-09", "20-12-25", "20-12-20", "20-12-21"].indexOf(currDate) != -1;
        },
        isCustomDate: function(e){
        // console.log(e);
        var dataCell = moment(e._d).format("YYYY-MM-DD");
            if ( dataCell == '2020-09-05' || dataCell == '2020-09-15' || dataCell == '2020-09-01' || dataCell == '2020-09-12' ) {
               console.log(dataCell)
                return 'isEvent';
            }
        }
        }, function(start, end, label) {
        console.log('New date range selected: ' + start.format('YYYY-MM-DD') + ' to ' + end.format('YYYY-MM-DD') + ' (predefined range: ' + label + ')');
        });
 }
//calendar initialization for mobile in english
 function initCalanderMinView(){
    $('#demo').daterangepicker({
        "parentEl": ".events",
        "singleDatePicker": true,
        "showDropdowns": true,
        "autoApply": true,
        "locale": {
            "format": "MM/DD/YYYY",
            "separator": " - ",
            "applyLabel": "Apply",
            "cancelLabel": "Cancel",
            "fromLabel": "From",
            "toLabel": "To",
            "customRangeLabel": "Custom",
            "weekLabel": "W",
            "daysOfWeek": [
                "Su",
                "Mo",
                "Tu",
                "We",
                "Th",
                "Fr",
                "Sa"
            ],
            "monthNames": [
                "January",
                "February",
                "March",
                "April",
                "May",
                "June",
                "July",
                "August",
                "September",
                "October",
                "November",
                "December"
            ],
            "firstDay": 1
        },
        "linkedCalendars": false,
        "showCustomRangeLabel": false,
        "alwaysShowCalendars": true,
        "startDate": today,
        //  "endDate": "09/16/2020",
        isInvalidDate: function(ele) {
          // console.log(ele);
        var currDate = moment(ele._d).format('YY-MM-DD');
        
        return ["20-09-09", "20-09-25", "20-09-20", "20-09-21"].indexOf(currDate) != -1;
        },
        isCustomDate: function(e){
        // console.log(e);
        var dataCell = moment(e._d).format("YYYY-MM-DD");
            if ( dataCell == '2020-09-05' || dataCell == '2020-09-15' || dataCell == '2020-09-01' || dataCell == '2020-09-12' ) {
               console.log(dataCell)
                return 'isEvent';
            }
        }
        }, function(start, end, label) {
        console.log('New date range selected: ' + start.format('YYYY-MM-DD') + ' to ' + end.format('YYYY-MM-DD') + ' (predefined range: ' + label + ')');
        });
 }

//  ======== calendar arabic version =======
//calendar initialization for dekstop in arabic
function initCalanderAr(){
    $('#demo').daterangepicker({
        "parentEl": ".events",
        "singleDatePicker": true,
        "showDropdowns": true,
        "autoApply": true,
        "locale": {
            "format": "MM/DD/YYYY",
            "separator": " - ",
            "applyLabel": "Apply",
            "cancelLabel": "Cancel",
            "fromLabel": "From",
            "toLabel": "To",
            "customRangeLabel": "Custom",
            "weekLabel": "W",
            "daysOfWeek": [
                "الأحد",
                "الاثنين",
                "الثلاثاء",
                "الأربعاء",
                "الخميس",
                "الجمعة",
                "السبت"
            ],
            "monthNames": [
                "يناير",
                "فبراير",
                "مارس",
                "إبريل",
                "مايو",
                "يونيو",
                "يوليو",
                "أغسطس",
                "سبتمبر",
                "أكتوبر",
                "نوفمبر",
                "ديسمبر"
            ],
            "firstDay": 1
        },
        "linkedCalendars": false,
        "showCustomRangeLabel": false,
        "alwaysShowCalendars": true,
        "startDate": today,
         "endDate": "12/12/2020",
        isInvalidDate: function(ele) {
          // console.log(ele);
        var currDate = moment(ele._d).format('YY-MM-DD');
        
        return ["20-12-09", "20-12-25", "20-12-20", "20-12-21"].indexOf(currDate) != -1;
        },
        isCustomDate: function(e){
        // console.log(e);
        var dataCell = moment(e._d).format("YYYY-MM-DD");
            if ( dataCell == '2020-09-05' || dataCell == '2020-09-15' || dataCell == '2020-09-01' || dataCell == '2020-09-12' ) {
               console.log(dataCell)
                return 'isEvent';
            }
        }
        }, function(start, end, label) {
        console.log('New date range selected: ' + start.format('YYYY-MM-DD') + ' to ' + end.format('YYYY-MM-DD') + ' (predefined range: ' + label + ')');
        });
        
 }

    /*************************************Calendar js close******************************************************************/
 
    ev_calendar() //display calendar on page load
 
 