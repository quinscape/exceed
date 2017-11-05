import React from "react"

import Nav from "react-bootstrap/lib/Nav"
import Navbar from "react-bootstrap/lib/Navbar"
import NavItem from "react-bootstrap/lib/NavItem"
import NavDropdown from "react-bootstrap/lib/NavDropdown"
import MenuItem from "react-bootstrap/lib/MenuItem"

import store from "../../../service/store"
import { getRoutingTable } from "../../../reducers/meta"

import uri from "../../../util/uri"
import sys from "../../../sys"
import i18n from "../../../service/i18n";

const CLASS_TOP = "top";
const CLASS_ADMIN = "admin";
const CLASS_HIDDEN = "hidden";

class StandardNav extends React.Component {

    render()
    {
        const { showBranding, showNormal, showAdmin, children } = this.props;


        const state = store.getState();

        const routingTable = getRoutingTable(state);
        const { mappings } = routingTable;

        const applications = [];
        const topLevelApplications = [];
        const adminApplications = [];

        for (let location in mappings)
        {

            if (mappings.hasOwnProperty(location))
            {
                const m = mappings[location];
                const { classes } = m;

                if (classes && classes.indexOf(CLASS_HIDDEN) >= 0)
                {
                    continue;
                }

                const id = m.viewName || m.processName;

                if (classes && classes.indexOf(CLASS_TOP) >= 0)
                {
                    topLevelApplications.push({
                        id,
                        location,
                        name: m.title || id,
                    });
                }
                else if (classes && classes.indexOf(CLASS_ADMIN) >= 0)
                {
                    adminApplications.push({
                        id,
                        location,
                        name: m.title || id
                    });
                }
                else
                {
                    applications.push({
                        id,
                        location,
                        name: m.title || id
                    });
                }
            }
        }

        return (
            <Navbar>
                <Navbar.Header>
                    {
                        showBranding &&
                        <Navbar.Brand>
                            {
                                React.Children.count(children) === 0 ?
                                    <a href={ uri( "/app/" + sys.appName ) }> { sys.appName }</a> :
                                    { children }
                            }
                        </Navbar.Brand>
                    }
                    <Navbar.Toggle />
                </Navbar.Header>
                <Nav>
                    {
                        showNormal && applications.length > 0 &&
                        <NavDropdown id="applicationDropdown" title={ i18n("App Nav") }>
                            {
                                applications.map(
                                    ({id, name,location}) =>

                                        <MenuItem key={ id } href={ uri("/app/" + sys.appName + location) }>{ name }</MenuItem>
                                )
                            }
                        </NavDropdown>
                    }
                    {
                        topLevelApplications.length > 0 &&
                        topLevelApplications.map(
                            ({id, name,location}) =>

                                <NavItem key={ id } href={ uri("/app/" + sys.appName + location) }>{ name }</NavItem>
                        )
                    }
                </Nav>
                <Nav pullRight>
                    {
                        showAdmin && !!adminApplications.length &&
                        <NavDropdown id="adminApplicationDropdown" title={ i18n("Admin Nav") }>
                            {
                                adminApplications.map(
                                    ({id, name,location}) =>

                                        <MenuItem key={ id } href={ uri("/app/" + sys.appName + location) }>{ name }</MenuItem>
                                )
                            }
                        </NavDropdown>
                    }
                </Nav>
            </Navbar>
        );
    }
}

export default StandardNav
