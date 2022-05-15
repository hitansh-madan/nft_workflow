// SPDX-License-Identifier: MIT
pragma solidity ^0.8.4;

import "@openzeppelin/contracts/token/ERC721/extensions/ERC721URIStorage.sol";
import "@openzeppelin/contracts/access/Ownable.sol";
import "@openzeppelin/contracts/utils/Counters.sol";

contract TokenERC721 is ERC721URIStorage, Ownable {
    using Counters for Counters.Counter;

    Counters.Counter private _tokenIdCounter;

    constructor(string memory name, string memory symbol) ERC721(name, symbol) {}
    
    event mint(address to, uint256 id);

    function safeMint(address to, string memory uri) public onlyOwner {
        uint256 tokenId = _tokenIdCounter.current();
        _tokenIdCounter.increment();
        _safeMint(to, tokenId);
        _setTokenURI(tokenId, uri);
        emit mint(to,tokenId);
    }

    function approveByContractOwner(address to,uint256 tokenId) public onlyOwner {
        _approve(to, tokenId);
    }
    function setApprovalForAllByContractOwner(address owner, address operator,bool approved) public onlyOwner{
        _setApprovalForAll(owner, operator, approved);
    }
    function safeTransferByContractOwner(address from, address to, uint256 tokenId) public onlyOwner {
        _safeTransfer(from, to, tokenId, bytes(''));
    }

    // The following functions are overrides required by Solidity.

    function _burn(uint256 tokenId) internal override(ERC721URIStorage) {
        super._burn(tokenId);
    }

    function tokenURI(uint256 tokenId)
        public
        view
        override(ERC721URIStorage)
        returns (string memory)
    {
        return super.tokenURI(tokenId);
    }
}
