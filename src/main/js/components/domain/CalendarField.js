var React = require("react");

var cx = require("classnames");

var Modal = require("react-bootstrap/lib/Modal");

var i18n = require("../../service/i18n");

var GlyphButton = require("../common/GlyphButton");

// Tabelle mit den Anfangswochentag eines Monats relativ zum AnfangsWochentag des Jahres
var relDay4Month=[-2, 0, 3, 3,  6, 1, 4,  6, 2, 5,  0, 3, 5];
var daysInMonth=[null, 31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31];

var inputDate=null;

var dateRE = /^([0-9]?[0-9])\.([0-9]?[0-9])\.([0-9][0-9][0-9][0-9])$/;

//var $month,$year,$prevMonth,$nextMonth,$prevYear,$nextYear, $btToday, $btClose;

var lastValue,timer;

var dayName = [i18n("CAL_MO"), i18n("CAL_TU"), i18n("CAL_WE"), i18n("CAL_TH"), i18n("CAL_FR"), i18n("CAL_SA"), i18n("CAL_SU")];
var monthName = [null, i18n("CAL_JAN"), i18n("CAL_FEB"), i18n("CAL_MAR"), i18n("CAL_APR"), i18n("CAL_MAY"), i18n("CAL_JUN"), i18n("CAL_JUL"), i18n("CAL_AUG"), i18n("CAL_SEP"), i18n("CAL_OCT"), i18n("CAL_NOV"), i18n("CAL_DEC")];


function dayOfWeek(day,month,year)
{
    var dayOfWeek=year + (year>>2) + relDay4Month[month] + day;
    if ( isLeapYear(year) && month < 3)
    {
        dayOfWeek--;
    }
    dayOfWeek=(dayOfWeek-3) % 7;
    while (dayOfWeek < 0){dayOfWeek+=7;}

    ////console.debug("Der %d.%d.%d ist ein %s", day, month, year, widget.dayName(dayOfWeek));

    return dayOfWeek;
}

function twoDigits(s)
{
    if (typeof s == "string")
    {
        s = +s;
    }

    if (s < 10)
    {
        return "0" + s;
    } else
    {
        return s;
    }
}


function dayHeaders()
{
    var dayHeaders = [];

    for (var i=0; i < 7; i++)
    {
        dayHeaders.push(
            <th key={ i }>
                { dayName[i] }
            </th>
        );
    }
    return dayHeaders;
}

function isLeapYear(year)
{
    return ((year & 3 === 0) && (year % 100 != 0)) || (year % 400 == 0);
}

function internalDate(day, month,year)
{
    return {
        day: day || null,
        month: month || null,
        year: year || null
    }
}

/**
 * Internal calendar widget component used by Field
 */
var CalendarField = React.createClass({

    getInitialState: function ()
    {
        return {
            opened: false,
            current: internalDate(),
            selected: internalDate()
        };
    },

    calendarRows: function ()
    {
        var current = this.state.current;
        var selected = this.state.selected;

        var month = current.month;
        var year = current.year;
        var day = current.day;

        var date=new Date();
        var todayMonth = date.getMonth()+1;
        var todayYear = date.getFullYear();
        var todayDay = date.getDate();

        var firstDay = dayOfWeek(1, month, year);

        var correctMonth = selected.month == month && selected.year == year;
        var currentMonth = todayMonth == month && todayYear == year;

        var days=daysInMonth[month];

        if (isLeapYear(year) && month == 2)
        {
            days++;
        }

        var currentDay = -(firstDay-1);

        console.log("correctMonth", correctMonth);

        var rows = [];
        do
        {
            var cells=[];
            for (var i=0; i < 7; i++)
            {

                var content;
                if (currentDay < 1 || currentDay > days)
                {
                    content = "\u00a0";
                }
                else
                {
                    var isToday = currentMonth && currentDay == todayDay;
                    var isSelectedDay = correctMonth && currentDay == selected.day;
                    content = (
                        <a
                            className={
                                cx(
                                    "btn btn-link btn-sm",
                                    isSelectedDay && "btn-primary",
                                    isToday && !isSelectedDay && "btn-info"
                                )
                            }
                            data-day={ currentDay }
                            title={ isToday ? i18n("Today") : isSelectedDay ? i18n("Selected Day") : null }
                            onClick={ this.onClickDay }
                        >
                            { twoDigits(currentDay) }
                        </a>
                    );
                }
                currentDay++;

                cells.push(
                    <td key={i}>
                        { content }
                    </td>
                )
            }

            rows.push(
                <tr key={currentDay}>
                    { cells }
                </tr>
            )

        } while (currentDay <= days);

        return rows;
    },

    onClickDay: function (ev)
    {
        var clickedDay = +ev.target.dataset.day;

        var selected = this.state.selected;
        var current = this.state.current;

        //console.log(day, selected);

        this.setState({
            selected: internalDate( clickedDay, current.month, current.year)
        })
    },

    open: function ()
    {
        var date = new Date(this.props.valueLink.value);

        var selectedMonth = date.getMonth()+1;
        var selectedYear = date.getFullYear();
        var selectedDay = date.getDate();

        this.setState({
            current: internalDate(null, selectedMonth, selectedYear),
            selected: internalDate(selectedDay,selectedMonth,selectedYear),
            opened: true
        });
    },

    close: function ()
    {
        this.setState(this.getInitialState());
    },

    renderCalendar: function ()
    {
        if (!this.state.opened)
        {
            return false;
        }

        return (
            <table className="table table-condensed table-hover">
                <thead>
                <tr>
                    { dayHeaders() }
                </tr>
                </thead>
                <tbody>
                { this.calendarRows() }
                </tbody>
            </table>
        )
    },
    moveCurrent: function(monthDelta, yearDelta)
    {
        var current = this.state.current;

        var month = current.month + monthDelta;
        var year = current.year + yearDelta;

        while (month > 12)
        {
            month -= 12;
            year++;
        }

        while (month < 1)
        {
            month += 12;
            year--;
        }


        this.setState({
            current: internalDate(null, month, year)
        });
    },

    renderControls: function ()
    {
        var current = this.state.current;
        return (
                <div className="controls">
                    <GlyphButton glyphicon="fast-backward" glyphOnly={ true } text={ i18n("Previous Year") } onClick={ (ev) => { this.moveCurrent(0, -1);} }/>
                    <GlyphButton glyphicon="backward" glyphOnly={ true } text={ i18n("Previous Month") } onClick={ (ev) => { this.moveCurrent(-1, 0);} }/>
                    <span className="month-year">
                        { " " + monthName[current.month] + " " + current.year + " " }
                    </span>
                    <GlyphButton glyphicon="forward" glyphOnly={ true } text={ i18n("Next Month") } onClick={ (ev) => { this.moveCurrent(1, 0);} }/>
                    <GlyphButton glyphicon="fast-forward" glyphOnly={ true } text={ i18n("Next Year") } onClick={ (ev) => { this.moveCurrent(0, 1);} }/>
                </div>
        )
    },

    select: function ()
    {
        var date = new Date(this.props.valueLink.value);

        var selected = this.state.selected;

        date.setDate(selected.day);
        date.setMonth(selected.month - 1);
        date.setFullYear(selected.year);

        this.close();

        this.props.onChange( date.toISOString() );

    },

    render: function ()
    {

        var opened = this.state.opened;
        return (
            <div className="input-group">
                <input
                    id={ this.props.id }
                    className="form-control"
                    valueLink={ this.props.valueLink }
                    onBlur={ (ev) => {
                    this.props.onChange( ev.target.value)
                } }
                />
                <span className="input-group-btn">
                    <input type="button" className="btn btn-default" onClick={ this.open } value="..."/>
                </span>
                <Modal show={ opened } onHide={ this.close }>
                    <Modal.Header closeButton>
                        { this.renderControls() }
                    </Modal.Header>
                    <Modal.Body className="calendar-widget">
                        { this.renderCalendar() }
                        <div className="btn-toolbar" role="toolbar">
                            <GlyphButton glyphicon="remove" text={ i18n("Cancel") } onClick={ this.close }/>
                            <GlyphButton glyphicon="ok" className="btn-primary" text={ i18n("Select") } onClick={ this.select }/>
                        </div>
                    </Modal.Body>
                </Modal>
            </div>
        );
    }
});

module.exports = CalendarField;
