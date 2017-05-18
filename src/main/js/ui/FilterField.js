const React = require("react");

import i18n from "../service/i18n";

/**
 * Internal editor filter field working with value links, not cursors.
 */
var FilterField = function (props)
    {
        return (
            <form className="form">
                <div className="form-group">
                    <label htmlFor="filter">Filter</label>
                    <div className="input-group">
                        <input
                            id="filter"
                            type="text"
                            className="form-control"
                            value={ props.valueLink.value }
                            onChange={ ev => props.valueLink.requestChange(ev.target.value) }
                            placeholder={ props.placeholder }
                        />
                        <span className="input-group-btn">
                            <button
                                type="button"
                                className="btn btn-default"
                                onClick={ e => this.setFilter("") }>{ i18n("Clear") }</button>
                        </span>
                    </div>
                </div>
            </form>
        );
};

export default FilterField
