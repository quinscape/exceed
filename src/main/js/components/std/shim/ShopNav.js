import React from "react"

import Nav from "react-bootstrap/lib/Nav"
import Navbar from "react-bootstrap/lib/Navbar"
import NavItem from "react-bootstrap/lib/NavItem"

import uri from "../../../util/uri"
import sys from "../../../sys"

class ShopNav extends React.Component {

    render()
    {
        return (
            <Navbar>
                <Navbar.Header>
                    <Navbar.Brand>
                        <a href={ uri("/") }>ACME Shipping</a>
                    </Navbar.Brand>
                    <Navbar.Toggle />
                </Navbar.Header>
                <Nav>
                    <NavItem href={ uri("/app/" + sys.appName + "/customers") }>Customers</NavItem>
                    <NavItem href={ uri("/app/" + sys.appName + "/products") }>Products</NavItem>
                    <NavItem href={ uri("/app/" + sys.appName + "/orders") }>Orders</NavItem>
                </Nav>
                <Nav pullRight>
                    <NavItem href={ uri("/app/" + sys.appName + "/admin") }>Administration</NavItem>
                </Nav>
            </Navbar>
        );
    }
}

export default ShopNav
