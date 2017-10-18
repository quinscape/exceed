import React from "react";
import cx from "classnames";
import Modal from "react-bootstrap/lib/Modal";
import i18n from "../../../service/i18n";
import GlyphButton from "../common/GlyphButton";

// Tabelle mit den Anfangswochentag eines Monats relativ zum AnfangsWochentag des Jahres
const relDay4Month = [-2, 0, 3, 3, 6, 1, 4, 6, 2, 5, 0, 3, 5];
const daysInMonth = [null, 31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31];

const inputDate = null;

const dateRE = /^([0-9]?[0-9])\.([0-9]?[0-9])\.([0-9][0-9][0-9][0-9])$/;

//var $month,$year,$prevMonth,$nextMonth,$prevYear,$nextYear, $btToday, $btClose;

let lastValue, timer;

const dayName = [i18n("CAL_MO"), i18n("CAL_TU"), i18n("CAL_WE"), i18n("CAL_TH"), i18n("CAL_FR"), i18n("CAL_SA"), i18n("CAL_SU")];
const monthName = [null, i18n("CAL_JAN"), i18n("CAL_FEB"), i18n("CAL_MAR"), i18n("CAL_APR"), i18n("CAL_MAY"), i18n("CAL_JUN"), i18n("CAL_JUL"), i18n("CAL_AUG"), i18n("CAL_SEP"), i18n("CAL_OCT"), i18n("CAL_NOV"), i18n("CAL_DEC")];

function dayOfWeek(day,month,year)
{
    let dayOfWeek = year + (year >> 2) + relDay4Month[month] + day;
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
    if (typeof s === "string")
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
    const dayHeaders = [];

    for (let i=0; i < 7; i++)
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
    return ((year & 3 === 0) && (year % 100 !== 0)) || (year % 400 === 0);
}

function internalDate(day, month,year)
{
    return {
        day: day || null,
        month: month || null,
        year: year || null
    }
}

const INITIAL_STATE = {
    opened: false,
    current: internalDate(),
    selected: internalDate()
};


    /**
 * Internal calendar widget component used by Field
 */
class CalendarField extends React.Component
{
    state = INITIAL_STATE;

    getInputField ()
    {
        return this._input;
    }


/*
    getInitialState ()
    {
        return {
            opened: false,
            current: internalDate(),
            selected: internalDate()
        };
    },
*/
    calendarRows ()
    {
        const current = this.state.current;
        const selected = this.state.selected;

        const month = current.month;
        const year = current.year;
        const day = current.day;

        const date = new Date();
        const todayMonth = date.getMonth() + 1;
        const todayYear = date.getFullYear();
        const todayDay = date.getDate();

        const firstDay = dayOfWeek(1, month, year);

        const correctMonth = selected.month === month && selected.year === year;
        const currentMonth = todayMonth === month && todayYear === year;

        let days = daysInMonth[month];

        if (isLeapYear(year) && month === 2)
        {
            days++;
        }

        let currentDay = -(firstDay - 1);

//        console.log("correctMonth", correctMonth);

        const rows = [];
        do
        {
            const cells = [];
            for (let i=0; i < 7; i++)
            {

                let content;
                if (currentDay < 1 || currentDay > days)
                {
                    content = "\u00a0";
                }
                else
                {
                    const isToday = currentMonth && currentDay === todayDay;
                    const isSelectedDay = correctMonth && currentDay === selected.day;
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
    }

    onClickDay = (ev) =>
    {
        const clickedDay = +ev.target.dataset.day;

        const selected = this.state.selected;
        const current = this.state.current;

        //console.log(day, selected);

        this.setState({
            selected: internalDate( clickedDay, current.month, current.year)
        })
    };

    open = () =>
    {
        let date = new Date(this.props.value);

        if (isNaN(date.valueOf()))
        {
//            console.log("DEFAULT DATE");
            date = new Date();
        }

        const selectedMonth = date.getMonth() + 1;
        const selectedYear = date.getFullYear();
        const selectedDay = date.getDate();

        this.setState({
            current: internalDate(null, selectedMonth, selectedYear),
            selected: internalDate(selectedDay,selectedMonth,selectedYear),
            opened: true
        });
    };

    close = () =>
    {
        this.setState(INITIAL_STATE);
    };

    renderCalendar ()
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
    }

    moveCurrent(monthDelta, yearDelta)
    {
        const current = this.state.current;

        let month = current.month + monthDelta;
        let year = current.year + yearDelta;

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
    }

    renderControls ()
    {
        const current = this.state.current;
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
    }

    select = () =>
    {
        const date = new Date(this.props.value);

        const selected = this.state.selected;

        date.setDate(selected.day);
        date.setMonth(selected.month - 1);
        date.setFullYear(selected.year);

        this.close();

        this.props.onChange( date.toISOString() );

    };

    onChange = ev => this.props.onChange(ev.target.value);

    render ()
    {
        const opened = this.state.opened;
        return (
            <div className="input-group">
                <input
                    id={ this.props.id }
                    ref={ elem => this._input = elem}
                    className="form-control"
                    value={ this.props.value }
                    onChange={ this.onChange }
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
};

export default CalendarField
